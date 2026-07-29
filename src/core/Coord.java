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

    /**
     * Returns the distance to another coordinate, for satellites positions
     * @param other The other coordinate
     * @return The distance between this and another coordinate
     */
    public double distance(Coord other) {
        /*OBTAINING THE LATITUDE AND LONGITUDE FROM THE
         INTERNAL REPRESENTATION COORDINATES FOR EACH POINT */
        int precisionFactor = 100;
        double thisLon = (this.x - 180) / precisionFactor;
        double thisLat = (90 - this.y) / precisionFactor;
        double otherLon = (other.x - 180) / precisionFactor;
        double otherLat = (90 - other.y) / precisionFactor;

        /*TRANSFORMING THE LAT LON DEGREES INTO RADS*/
        double thisLonRad = (thisLon * Math.PI) / 180;
        double thisLatRad = (thisLat * Math.PI) / 180;
        double otherLonRad = (otherLon * Math.PI) / 180;
        double otherLatRad = (otherLat * Math.PI) / 180;

        float rEarth = 6370;
        float altitude = 800;
        float r = rEarth + altitude;

        /*GENERATING CARTESIAN POSITION AND CALCULATING DELTA*/
        double thisX = r * Math.cos(thisLatRad) * Math.cos(thisLonRad);
        double otherX = r * Math.cos(otherLatRad) * Math.cos(otherLonRad);
        double thisY = r * Math.cos(thisLatRad) * Math.sin(thisLonRad);
        double otherY = r * Math.cos(otherLatRad) * Math.sin(otherLonRad);
        double thisZ = r * Math.sin(thisLatRad);
        double otherZ = r * Math.sin(otherLatRad);
        double dX = thisX - otherX;
        double dY = thisY - otherY;
        double dZ = thisZ - otherZ;

        double distance = Math.sqrt(dX*dX + dY*dY + dZ*dZ);

        return distance;
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
