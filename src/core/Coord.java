/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package core;

/**
 * Class to hold 2D coordinates and perform simple arithmetics and
 * transformations
 */
public class Coord implements Cloneable, Comparable<Coord> {
	private double x;
	private double y;

	/**
	 * Constructor.
	 * @param x Initial X-coordinate
	 * @param y Initial Y-coordinate
	 */
	public Coord(double x, double y) {
		setLocation(x,y);
	}

	/**
	 * Sets the location of this coordinate object
	 * @param x The x coordinate to set
	 * @param y The y coordinate to set
	 */
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Sets this coordinate's location to be equal to other
	 * coordinates location
	 * @param c The other coordinate
	 */
	public void setLocation(Coord c) {
		this.x = c.x;
		this.y = c.y;
	}

	/**
	 * Moves the point by dx and dy
	 * @param dx How much to move the point in X-direction
	 * @param dy How much to move the point in Y-direction
	 */
	public void translate(double dx, double dy) {
		this.x += dx;
		this.y += dy;
	}

	/**
	 * Returns the distance to another coordinate
	 * @param other The other coordinate
	 * @return The distance between this and another coordinate
	 */
	public double distanceOriginal(Coord other) {
		double dx = this.x - other.x;
		double dy = this.y - other.y;

		return Math.sqrt(dx*dx + dy*dy);
	}

    /** Earth radius (km) */
    public static final double EARTH_RADIUS = 6370;
    /** Default altitude (km) of a node over the Earth surface */
    public static final double DEFAULT_ALTITUDE = 800;
    /** Internal coordinates are geographic degrees multiplied by this */
    public static final int PRECISION_FACTOR = 100;

    /**
     * Returns the distance to another coordinate, for satellites positions.
     * Both points are assumed to be at {@link #DEFAULT_ALTITUDE}.
     * @param other The other coordinate
     * @return The distance between this and another coordinate
     */
    public double distance(Coord other) {
        return distance(other, DEFAULT_ALTITUDE, DEFAULT_ALTITUDE);
    }

    /**
     * Returns the straight line (3D) distance to another coordinate, each
     * point placed at its own altitude over the Earth surface
     * @param other The other coordinate
     * @param thisAltitude Altitude (km) of this point
     * @param otherAltitude Altitude (km) of the other point
     * @return The distance (km) between this and another coordinate
     */
    public double distance(Coord other, double thisAltitude,
            double otherAltitude) {
        double[] thisPos = toCartesian(EARTH_RADIUS + thisAltitude);
        double[] otherPos = other.toCartesian(EARTH_RADIUS + otherAltitude);
        double dX = thisPos[0] - otherPos[0];
        double dY = thisPos[1] - otherPos[1];
        double dZ = thisPos[2] - otherPos[2];

        return Math.sqrt(dX*dX + dY*dY + dZ*dZ);
    }

    /**
     * Returns the distance over the Earth surface (great circle) between the
     * points right below this coordinate and another one, ignoring altitude
     * @param other The other coordinate
     * @return The surface distance (km) between both points
     */
    public double surfaceDistance(Coord other) {
        /*CHORD BETWEEN BOTH POINTS ON A UNIT SPHERE GIVES THE CENTRAL ANGLE*/
        double[] thisPos = toCartesian(1);
        double[] otherPos = other.toCartesian(1);
        double dX = thisPos[0] - otherPos[0];
        double dY = thisPos[1] - otherPos[1];
        double dZ = thisPos[2] - otherPos[2];
        double chord = Math.sqrt(dX*dX + dY*dY + dZ*dZ);
        double angle = 2 * Math.asin(Math.min(1, chord / 2));

        return EARTH_RADIUS * angle;
    }

    /**
     * Creates a coordinate from geographic degrees. Longitudes out of the
     * [-180, 180] range give coordinates out of the map, which is useful for
     * drawing shapes that cross the map border.
     * @param lon Longitude (degrees)
     * @param lat Latitude (degrees)
     * @return The coordinate in the internal representation
     */
    public static Coord fromGeographic(double lon, double lat) {
        return new Coord((lon + 180) * PRECISION_FACTOR,
                (90 - lat) * PRECISION_FACTOR);
    }

    /**
     * Returns the longitude of this coordinate
     * @return Longitude (degrees)
     */
    public double getLongitude() {
        return this.x / PRECISION_FACTOR - 180;
    }

    /**
     * Returns the latitude of this coordinate
     * @return Latitude (degrees)
     */
    public double getLatitude() {
        return 90 - this.y / PRECISION_FACTOR;
    }

    /**
     * Converts this coordinate to Earth centered cartesian coordinates
     * @param r Distance (km) from the Earth center
     * @return Array with the x, y and z cartesian coordinates
     */
    private double[] toCartesian(double r) {
        /*TRANSFORMING THE LAT LON DEGREES INTO RADS*/
        double lonRad = (getLongitude() * Math.PI) / 180;
        double latRad = (getLatitude() * Math.PI) / 180;

        return new double[] {
            r * Math.cos(latRad) * Math.cos(lonRad),
            r * Math.cos(latRad) * Math.sin(lonRad),
            r * Math.sin(latRad)
        };
    }

	/**
	 * Returns the x coordinate
	 * @return x coordinate
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * Returns the y coordinate
	 * @return y coordinate
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * Returns a text representation of the coordinate (rounded to 2 decimals)
	 * @return a text representation of the coordinate
	 */
	public String toString() {
		return String.format("(%.2f,%.2f)",x,y);
	}

	/**
	 * Returns a clone of this coordinate
	 */
	public Coord clone() {
		Coord clone = null;
		try {
			clone = (Coord) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return clone;
	}

	/**
	 * Checks if this coordinate's location is equal to other coordinate's
	 * @param c The other coordinate
	 * @return True if locations are the same
	 */
	public boolean equals(Coord c) {
		if (c == this) {
			return true;
		}
		else {
			return (x == c.x && y == c.y); // XXX: == for doubles...
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		return equals((Coord) o);
	}

	/**
	 * Returns a hash code for this coordinate
	 * (actually a hash of the String made of the coordinates)
	 */
	public int hashCode() {
		return (x+","+y).hashCode();
	}

	/**
	 * Compares this coordinate to other coordinate. Coordinate whose y
	 * value is smaller comes first and if y values are equal, the one with
	 * smaller x value comes first.
	 * @return -1, 0 or 1 if this node is before, in the same place or
	 * after the other coordinate
	 */
	public int compareTo(Coord other) {
		if (this.y < other.y) {
			return -1;
		}
		else if (this.y > other.y) {
			return 1;
		}
		else if (this.x < other.x) {
			return -1;
		}
		else if (this.x > other.x) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
