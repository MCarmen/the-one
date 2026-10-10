/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package gui.playfield;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import core.Connection;
import core.Coord;
import core.DTNHost;
import core.NetworkInterface;

/**
 * Visualization of a DTN Node
 *
 */
public class NodeGraphic extends PlayFieldGraphic {
	private static boolean drawCoverage;
	private static boolean drawNodeName;
	private static boolean drawConnections;
	private static boolean drawBuffer;
	private static List<DTNHost> highlightedNodes;

	private static Color rangeColor = Color.GREEN;
	private static Color conColor = Color.BLACK;
	private static Color hostColor = Color.BLUE;
	private static Color hostNameColor = Color.BLUE;
	private static Color msgColor1 = Color.BLUE;
	private static Color msgColor2 = Color.GREEN;
	private static Color msgColor3 = Color.RED;

	private static Color highlightedNodeColor = Color.MAGENTA;

	/** number of points of the coverage area outline */
	private static final int COVERAGE_POINTS = 72;

	private DTNHost node;

	public NodeGraphic(DTNHost node) {
		this.node = node;
	}

	@Override
	public void draw(Graphics2D g2) {
		drawHost(g2);
		if (drawBuffer) {
			drawMessages(g2);
		}
	}

	/**
	 * @return true if the node this graphic represents should be highlighted
	 */
	private boolean isHighlighted() {
		if (highlightedNodes == null) {
			return false;
		} else {
			return highlightedNodes.contains(node);
		}
	}

	/**
	 * Returns the outline on the map of the area covered by an interface of
	 * this node. The area is a spherical cap around the node: for a ground
	 * link interface, the points below the satellites that the ground node
	 * sees over the minimum elevation, inside the cone of the satellite
	 * antenna and within the range (for a ground node, satellites at
	 * {@link Coord#DEFAULT_ALTITUDE}); otherwise, the
	 * points at the node's altitude within the range and in line of sight.
	 * @param ni The network interface
	 * @param lonOffset Longitude (degrees) to shift the outline
	 * @return The outline of the covered area
	 */
	private Path2D.Double coverageArea(NetworkInterface ni, double lonOffset) {
		Coord loc = node.getLocation();
		double range = ni.getTransmitRange();
		double earthR = Coord.EARTH_RADIUS;
		double angle; // central angle (rad) between the node and the border

		if (ni.isGroundLink()) {
			double r = earthR + (node.isGroundNode() ?
					Coord.DEFAULT_ALTITUDE : node.getAltitude());
			double minElev = Math.toRadians(ni.getMinElevation());
			double elevAngle = Math.acos(earthR * Math.cos(minElev) / r) -
					minElev;
			/* central angle of the border of the antenna cone, or of the
			 * horizon if the cone is wider than the Earth seen from r */
			double beam = Math.toRadians(ni.getBeamAngle());
			double sinBorder = r * Math.sin(beam) / earthR;
			double beamAngle = sinBorder >= 1 ? Math.acos(earthR / r) :
					Math.asin(sinBorder) - beam;
			/* central angle where the slant distance equals the range */
			double cosRange = (earthR*earthR + r*r - range*range) /
					(2 * earthR * r);
			double rangeAngle = Math.acos(Math.max(-1, Math.min(1, cosRange)));
			angle = Math.min(elevAngle, Math.min(beamAngle, rangeAngle));
		} else {
			double r = earthR + node.getAltitude();
			double rangeAngle = 2 * Math.asin(Math.min(1, range / (2 * r)));
			double sightAngle = 2 * Math.acos(Math.min(1, earthR / r));
			angle = Math.min(rangeAngle, sightAngle);
		}
		angle = Math.min(angle, Math.PI);

		double lat1 = Math.toRadians(loc.getLatitude());
		double lon1 = loc.getLongitude() + lonOffset;
		Path2D.Double outline = new Path2D.Double();
		double firstLon = 0;
		double lon = 0;

		/* destination point for each bearing from the node */
		for (int i = 0; i <= COVERAGE_POINTS; i++) {
			double bearing = 2 * Math.PI * i / COVERAGE_POINTS;
			double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angle) +
					Math.cos(lat1) * Math.sin(angle) * Math.cos(bearing));
			double dLon = Math.toDegrees(Math.atan2(
					Math.sin(bearing) * Math.sin(angle) * Math.cos(lat1),
					Math.cos(angle) - Math.sin(lat1) * Math.sin(lat2)));
			double newLon = lon1 + dLon;

			/* keep the longitude continuous instead of jumping at +-180 */
			if (i > 0) {
				while (newLon - lon > 180) newLon -= 360;
				while (newLon - lon < -180) newLon += 360;
			}
			lon = newLon;
			Coord c = Coord.fromGeographic(lon, Math.toDegrees(lat2));

			if (i == 0) {
				firstLon = lon;
				outline.moveTo(scale(c.getX()), scale(c.getY()));
			} else {
				outline.lineTo(scale(c.getX()), scale(c.getY()));
			}
		}

		/* if the area contains a pole, the outline goes around the whole
		 * map and is left open: its copies at +-360 continue it */
		if (Math.abs(lon - firstLon) < 180) {
			outline.closePath();
		}

		return outline;
	}

	/**
	 * Visualize node's location, radio ranges and connections
	 * @param g2 The graphic context to draw to
	 */
	private void drawHost(Graphics2D g2) {
		Coord loc = node.getLocation();

		if (drawCoverage && node.isRadioActive()) {
			ArrayList<NetworkInterface> interfaces =
				new ArrayList<NetworkInterface>();
			interfaces.addAll(node.getInterfaces());
			// copies out of the map must not be seen
			Shape oldClip = g2.getClip();
			Coord mapEnd = Coord.fromGeographic(180, -90);
			g2.clipRect(0, 0, scale(mapEnd.getX()), scale(mapEnd.getY()));

			for (NetworkInterface ni : interfaces) {
				// draw the "range" area, also its copies at both sides of
				// the map for the parts that cross the +-180 longitude border
				g2.setColor(rangeColor);
				for (int copy = -1; copy <= 1; copy++) {
					g2.draw(coverageArea(ni, copy * 360));
				}
			}
			g2.setClip(oldClip);
		}

		if (drawConnections) {
			g2.setColor(conColor);
			Coord c1 = node.getLocation();
			Coord mapEnd = Coord.fromGeographic(180, -90);
			double mapWidth = mapEnd.getX();
			ArrayList<Connection> conList = new ArrayList<Connection>();
			// parts of the lines out of the map must not be seen
			Shape oldClip = g2.getClip();
			g2.clipRect(0, 0, scale(mapEnd.getX()), scale(mapEnd.getY()));
			// create a copy to prevent concurrent modification exceptions
			conList.addAll(node.getConnections());
			for (Connection c : conList) {
				DTNHost otherNode = c.getOtherNode(node);
				Coord c2;

				if (otherNode == null) {
					continue; /* disconnected before drawn */
				}
				c2 = otherNode.getLocation();
				double dx = c2.getX() - c1.getX();

				if (Math.abs(dx) <= mapWidth / 2) {
					g2.drawLine(scale(c1.getX()), scale(c1.getY()),
							scale(c2.getX()), scale(c2.getY()));
					continue;
				}
				// the short way crosses the +-180 longitude border: draw
				// the line from both ends, each one leaving the map
				double shift = Math.signum(dx) * mapWidth;
				g2.drawLine(scale(c1.getX()), scale(c1.getY()),
						scale(c2.getX() - shift), scale(c2.getY()));
				g2.drawLine(scale(c1.getX() + shift), scale(c1.getY()),
						scale(c2.getX()), scale(c2.getY()));
			}
			g2.setClip(oldClip);
		}


		/* draw node rectangle */
		g2.setColor(hostColor);
		g2.drawRect(scale(loc.getX()-1),scale(loc.getY()-1),
		scale(2),scale(2));

		if (isHighlighted()) {
			g2.setColor(highlightedNodeColor);
			g2.fillRect(scale(loc.getX()) - 3 ,scale(loc.getY()) - 3, 6, 6);
		}

		if (drawNodeName) {
			g2.setColor(hostNameColor);
			// Draw node's address next to it
			g2.drawString(node.toString(), scale(loc.getX()),
					scale(loc.getY()));
		}
	}

	/**
	 * Sets whether radio coverage of nodes should be drawn
	 * @param draw If true, radio coverage is drawn
	 */
	public static void setDrawCoverage(boolean draw) {
		drawCoverage = draw;
	}

	/**
	 * Sets whether node's name should be displayed
	 * @param draw If true, node's name is displayed
	 */
	public static void setDrawNodeName(boolean draw) {
		drawNodeName = draw;
	}

	/**
	 * Sets whether node's connections to other nodes should be drawn
	 * @param draw If true, node's connections to other nodes is drawn
	 */
	public static void setDrawConnections(boolean draw) {
		drawConnections = draw;
	}

	/**
	 * Sets whether node's message buffer is shown
	 * @param draw If true, node's message buffer is drawn
	 */
	public static void setDrawBuffer(boolean draw) {
		drawBuffer = draw;
	}

	public static void setHighlightedNodes(List<DTNHost> nodes) {
		highlightedNodes = nodes;
	}

	/**
	 * Visualize the messages this node is carrying
	 * @param g2 The graphic context to draw to
	 */
	private void drawMessages(Graphics2D g2) {
		int nrofMessages = node.getNrofMessages();
		Coord loc = node.getLocation();

		drawBar(g2,loc, nrofMessages % 10, 1);
		drawBar(g2,loc, nrofMessages / 10, 2);
	}

	/**
	 * Draws a bar (stack of squares) next to a location
	 * @param g2 The graphic context to draw to
	 * @param loc The location where to draw
	 * @param nrof How many squares in the stack
	 * @param col Which column
	 */
	private void drawBar(Graphics2D g2, Coord loc, int nrof, int col) {
		final int BAR_HEIGHT = 5;
		final int BAR_WIDTH = 5;
		final int BAR_DISPLACEMENT = 2;

		// draws a stack of squares next loc
		for (int i=1; i <= nrof; i++) {
			if (i%2 == 0) { // use different color for every other msg
				g2.setColor(msgColor1);
			}
			else {
				if (col > 1) {
					g2.setColor(msgColor3);
				}
				else {
					g2.setColor(msgColor2);
				}
			}

			g2.fillRect(scale(loc.getX()-BAR_DISPLACEMENT-(BAR_WIDTH*col)),
					scale(loc.getY()- BAR_DISPLACEMENT- i* BAR_HEIGHT),
					scale(BAR_WIDTH), scale(BAR_HEIGHT));
		}

	}

}
