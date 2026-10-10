/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement;

import input.ExternalMovementReader;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import util.Tuple;

import core.Coord;
import core.DTNSim;
import core.Settings;
import core.SimClock;

/**
 * Movement model that uses external data of node locations.
 */
public class ExternalMovement extends MovementModel {
	/** Namespace for settings */
	public static final String EXTERNAL_MOVEMENT_NS = "ExternalMovement";
	/** external locations file's path -setting id ({@value})*/
	public static final String MOVEMENT_FILE_S = "file";
	/** number of preloaded intervals per preload run -setting id ({@value})*/
	public static final String NROF_PRELOAD_S = "nrofPreload";
	/** does the external locations file have the altitude (km) of each
	 * location after its x and y -setting id ({@value}). Default is false */
	public static final String ALTITUDE_IN_FILE_S = "altitudeInFile";
	/** altitude (km) of the group's nodes when the location data has no
	 * altitude -setting id ({@value}), read from the group's namespace.
	 * Default is 0 (ground nodes). */
	public static final String ALTITUDE_S = "altitude";

	/** default initial location for excess nodes */
	private static final Coord DEF_INIT_LOC = new Coord(0,0);
	private static ExternalMovementReader reader;
	private static String inputFileName;

	/** mapping of external id to movement model */
	private static Map<String, ExternalMovement> idMapping;
	/** initial locations for nodes */
	private static List<Tuple<String, Coord>> initLocations;
	/** time of the very first location data */
	private static double initTime;
	/** sampling interval (seconds) of the location data */
	private static double samplingInterval;
	/** last read time stamp after preloading */
	private static double lastPreloadTime;
	/** how many time intervals to load on every preload run */
	private static double nrofPreload = 10;
	/** minimum number intervals that should be preloaded ahead of sim time */
	private static final double MIN_AHEAD_INTERVALS = 2;

	/** the very first location of the node */
	private Coord intialLocation;
	/** queue of path-start-time, path tuples */
	private Queue<Tuple<Double, Path>> pathQueue;

	/** when was the path currently under construction started */
	private double latestPathStartTime;
	/** the last location of path waypoint */
	private Coord latestLocation;
	/** the path currently under construction */
	private Path latestPath;

	/** is this node active */
	private boolean isActive;
	/** altitude (km) for the locations that have none */
	private double altitude;

	static {
		DTNSim.registerForReset(ExternalMovement.class.getCanonicalName());
		reset();
	}

	/**
	 * Constructor for the prototype. Run once per group.
	 * @param settings Where settings are read from
	 */
	public ExternalMovement(Settings settings) {
		super(settings);
		this.altitude = settings.getDouble(ALTITUDE_S, 0);

		if (idMapping == null) {
			// run these the first time object is created or after reset call
			Settings s = new Settings(EXTERNAL_MOVEMENT_NS);
			idMapping = new HashMap<String, ExternalMovement>();
			inputFileName = s.getSetting(MOVEMENT_FILE_S);
			reader = new ExternalMovementReader(inputFileName);
			reader.setReadAltitude(s.getBoolean(ALTITUDE_IN_FILE_S, false));

			initLocations = reader.readNextMovements();
			initTime = reader.getLastTimeStamp();
			samplingInterval = -1;
			lastPreloadTime = -1;

			s.setNameSpace(EXTERNAL_MOVEMENT_NS);
			if (s.contains(NROF_PRELOAD_S)) {
				nrofPreload = s.getInt(NROF_PRELOAD_S);
				if (nrofPreload <= 0) {
					nrofPreload = 1;
				}
			}
		}
	}

	/**
	 * Copy constructor. Gives out location data for the new node from
	 * location queue.
	 * @param mm The movement model to copy from
	 */
	private ExternalMovement(ExternalMovement mm) {
		super(mm);

		pathQueue = new LinkedList<Tuple<Double, Path>>();
		latestPath = null;
		this.altitude = mm.altitude;

		if (initLocations.size() > 0) { // we have location data left
			// gets a new location from the list
			Tuple<String, Coord> initLoc = initLocations.remove(0);
			this.intialLocation = this.latestLocation =
				withAltitude(initLoc.getValue());
			this.latestPathStartTime = initTime;

			// puts the new model to model map for later updates
			idMapping.put(initLoc.getKey(), this);
			isActive = true;
		}
		else {
			// no more location data left for the new node -> set inactive
			this.intialLocation = DEF_INIT_LOC;
			isActive = false;
		}
	}

	/**
	 * Checks if more paths should be preloaded and preloads them if
	 * needed.
	 */
	private static void checkPathNeed() {
		if (samplingInterval == -1) { // first preload
			lastPreloadTime = readMorePaths();
		}

		if (!Double.isNaN(lastPreloadTime) && SimClock.getTime() >=
				lastPreloadTime - (samplingInterval * MIN_AHEAD_INTERVALS) ) {
			for (int i=0; i < nrofPreload &&
					!Double.isNaN(lastPreloadTime); i++) {
				lastPreloadTime = readMorePaths();
			}
		}
	}

	@Override
	public Coord getInitialLocation() {
		return this.intialLocation;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Adds a new location with a time to this model's move pattern. If the
	 * node stayed stationary during the update, the current path is put to the
	 * queue and a new path is started once the node starts moving.
	 * @param loc The location
	 * @param time When should the node be there
	 */
	private void addLocation(Coord loc, double time) {
		assert samplingInterval > 0 : "Non-positive sampling interval!";
		loc = withAltitude(loc);

		if (loc.equals(latestLocation)) { // node didn't move
			if (latestPath != null) {
				// constructing path -> end constructing and put it in the queue
				pathQueue.add(new Tuple<Double, Path>
					(latestPathStartTime, latestPath));
				latestPath = null;
			}

			this.latestPathStartTime = time;
			return;
		}

		if (latestPath == null) {
			latestPath = new Path();
		}

		double speed = loc.distance(this.latestLocation) / samplingInterval;
		latestPath.addWaypoint(loc, speed);

		this.latestLocation = loc;
	}

	/**
	 * Gives the altitude of this node's group to a location read without
	 * altitude
	 * @param loc The location read from the file
	 * @return The location with its altitude set
	 */
	private Coord withAltitude(Coord loc) {
		if (!reader.hasAltitude()) {
			return new Coord(loc.getX(), loc.getY(), this.altitude);
		}
		return loc;
	}

	/**
	 * Returns a sim time when the next path is available.
	 * @return The sim time when node should ask the next time for a path
	 */
	@Override
	public double nextPathAvailable() {
		if (pathQueue.size() == 0) {
			return latestPathStartTime;
		}
		else {
			return pathQueue.element().getKey();
		}
	}

	@Override
	public Path getPath() {
		Path p;

		checkPathNeed(); // check if we should preload more paths

		if (SimClock.getTime() < this.nextPathAvailable()) {
			return null;
		}

		if (pathQueue.size() == 0) { // nothing in the queue, return latest
			p = latestPath;
			latestPath = null;
		}
		else {	// return first path in the queue
			p = pathQueue.remove().getValue();
		}

		return p;
	}

	@Override
	public int getMaxX() {
		return (int)(reader.getMaxX() - reader.getMinX()) + 1;
	}

	@Override
	public int getMaxY() {
		return (int)(reader.getMaxY() - reader.getMinY()) + 1;
	}


	@Override
	public MovementModel replicate() {
		return new ExternalMovement(this);
	}

	/**
	 * Reads paths for the next time instance from the reader
	 * @return The time stamp of the reading or Double.NaN if no movements
	 * were read.
	 */
	private static double readMorePaths() {
		List<Tuple<String, Coord>> list = reader.readNextMovements();
		double time = reader.getLastTimeStamp();

		if (samplingInterval == -1) {
			samplingInterval = time - initTime;
		}

		for (Tuple<String, Coord> t : list) {
			ExternalMovement em = idMapping.get(t.getKey());
			if (em != null) { // skip unknown IDs, i.e. IDs not mentioned in...
				// ...init phase or if there are more IDs than nodes
				em.addLocation(t.getValue(), time);
			}
		}

		if (list.size() > 0) {
			return time;
		}
		else {
			return Double.NaN;
		}
	}

	/**
	 * Reset state so that next instance will have a fresh state
	 */
	public static void reset() {
		idMapping = null;
	}

}
