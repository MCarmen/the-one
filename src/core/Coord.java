/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package core;

/**
 * Class to hold 3D coordinates and perform simple arithmetics and
 * transformations. X and Y are the longitude and latitude on the map (see
 * {@link #fromGeographic(double, double)}) and Z is the altitude (km) over
 * the Earth surface.
 */
public class Coord implements Cloneable, Comparable<Coord> {
	private double x;
	private double y;
	private double z;

	/**
	 * Constructor for a coordinate on the Earth surface (altitude 0).
	 * @param x Initial X-coordinate
	 * @param y Initial Y-coordinate
	 */
	public Coord(double x, double y) {
		this(x, y, 0);
	}

	/**
	 * Constructor.
	 * @param x Initial X-coordinate
	 * @param y Initial Y-coordinate
	 * @param z Initial Z-coordinate (altitude in km)
	 */
	public Coord(double x, double y, double z) {
		setLocation(x, y, z);
	}

	/**
	 * Sets the location of this coordinate object, keeping its altitude
	 * @param x The x coordinate to set
	 * @param y The y coordinate to set
	 */
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Sets the location of this coordinate object
	 * @param x The x coordinate to set
	 * @param y The y coordinate to set
	 * @param z The z coordinate (altitude in km) to set
	 */
	public void setLocation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Sets this coordinate's location to be equal to other
	 * coordinates location
	 * @param c The other coordinate
	 */
	public void setLocation(Coord c) {
		this.x = c.x;
		this.y = c.y;
		this.z = c.z;
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
	 * Moves the point by dx, dy and dz
	 * @param dx How much to move the point in X-direction
	 * @param dy How much to move the point in Y-direction
	 * @param dz How much to move the point in Z-direction (altitude)
	 */
	public void translate(double dx, double dy, double dz) {
		translate(dx, dy);
		this.z += dz;
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
    /** Reference altitude (km) of a satellite over the Earth surface, used
     * when there is no satellite to take it from (e.g., to draw the area
     * covered by a ground station) */
    public static final double DEFAULT_ALTITUDE = 800;
    /** Internal coordinates are geographic degrees multiplied by this */
    public static final int PRECISION_FACTOR = 100;

    /**
     * Returns the straight line (3D) distance to another coordinate, each
     * point placed at its own altitude over the Earth surface
     * @param other The other coordinate
     * @return The distance (km) between this and another coordinate
     */
    public double distance(Coord other) {
        double[] thisPos = toCartesian();
        double[] otherPos = other.toCartesian();
        double dX = thisPos[0] - otherPos[0];
        double dY = thisPos[1] - otherPos[1];
        double dZ = thisPos[2] - otherPos[2];

        return Math.sqrt(dX*dX + dY*dY + dZ*dZ);
    }

    /**
     * Returns the elevation of another coordinate seen from this one: the
     * angle between the local horizon of this point and the straight line
     * to the other point
     * @param other The coordinate that is seen
     * @return The elevation (degrees), negative if it is below the horizon
     */
    public double elevation(Coord other) {
        double[] thisPos = toCartesian();
        double[] otherPos = other.toCartesian();
        double r = EARTH_RADIUS + this.z;
        double dist2 = 0;
        double up = 0;

        /*SINE OF THE ELEVATION IS THE COMPONENT OF THE LINE OF SIGHT ALONG
         *THE LOCAL VERTICAL (THE DIRECTION FROM THE EARTH CENTER)*/
        for (int i = 0; i < 3; i++) {
            double d = otherPos[i] - thisPos[i];
            dist2 += d * d;
            up += d * thisPos[i] / r;
        }
        if (dist2 == 0) {
            return 90;
        }
        double sin = up / Math.sqrt(dist2);
        return Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, sin))));
    }

    /**
     * Returns the nadir angle of another coordinate seen from this one: the
     * angle between the direction to the Earth center (nadir) and the
     * straight line to the other point. For a satellite, it tells if a point
     * is inside the cone covered by an antenna pointing to the nadir.
     * @param other The coordinate that is seen
     * @return The nadir angle (degrees), between 0 and 180
     */
    public double nadirAngle(Coord other) {
        double[] thisPos = toCartesian();
        double[] otherPos = other.toCartesian();
        double r = EARTH_RADIUS + this.z;
        double dist2 = 0;
        double down = 0;

        /*COSINE OF THE NADIR ANGLE IS THE COMPONENT OF THE LINE OF SIGHT
         *TOWARDS THE EARTH CENTER*/
        for (int i = 0; i < 3; i++) {
            double d = otherPos[i] - thisPos[i];
            dist2 += d * d;
            down -= d * thisPos[i] / r;
        }
        if (dist2 == 0) {
            return 0;
        }
        double cos = down / Math.sqrt(dist2);
        return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, cos))));
    }

    /**
     * Returns true if the straight line between this coordinate and another
     * one does not cross the Earth
     * @param other The other coordinate
     * @return True if there is line of sight between both points
     */
    public boolean hasLineOfSight(Coord other) {
        double[] p = toCartesian();
        double[] q = other.toCartesian();
        double[] d = {q[0] - p[0], q[1] - p[1], q[2] - p[2]};
        double dd = d[0]*d[0] + d[1]*d[1] + d[2]*d[2];

        if (dd == 0) {
            return true;
        }
        /*POINT OF THE SEGMENT CLOSEST TO THE EARTH CENTER*/
        double t = -(p[0]*d[0] + p[1]*d[1] + p[2]*d[2]) / dd;
        t = Math.max(0, Math.min(1, t));
        double minDist = 0;
        for (int i = 0; i < 3; i++) {
            double c = p[i] + t * d[i];
            minDist += c * c;
        }
        // small tolerance so points on the surface can see each other
        return Math.sqrt(minDist) >= EARTH_RADIUS - 1e-6;
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
     * @return Array with the x, y and z cartesian coordinates (km)
     */
    private double[] toCartesian() {
        return toCartesian(EARTH_RADIUS + this.z);
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
	 * Returns the z coordinate
	 * @return z coordinate (altitude in km over the Earth surface)
	 */
	public double getZ() {
		return this.z;
	}

	/**
	 * Returns a text representation of the coordinate (rounded to 2
	 * decimals). The z coordinate is only shown if it is not 0.
	 * @return a text representation of the coordinate
	 */
	public String toString() {
		if (z == 0) {
			return String.format("(%.2f,%.2f)",x,y);
		}
		return String.format("(%.2f,%.2f,%.2f)",x,y,z);
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
			return (x == c.x && y == c.y && z == c.z); // XXX: == for doubles...
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
	 * smaller x value comes first, and then the one with smaller z value.
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
		else if (this.z < other.z) {
			return -1;
		}
		else if (this.z > other.z) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
