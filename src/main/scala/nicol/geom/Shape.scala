package nicol
package geom

import math._

sealed trait Shape extends Immutable {
  def bounds: AABox

  def area: Float = 0

  def transposed(v: Vector): Shape
}

case class Line(start: Vector, end: Vector) extends Shape {
  /**
   * This line as a [Vector]
   */
  val vec = end - start

  lazy val bounds = {
    val r = scala.math.max(start.length, end.length)
    AABox(r * 2, r * 2)
  }

  override def toString = "[Line: " + start + " to " + end + "]"

  def transposed(v: Vector) = Line(start + v, end + v)

  def transform(m: Matrix) = Line(m * start, m * end)

  /**
   * Tests if this line intersects with a given line.
   *
   * @param that Line to test
   * @return None, if there was no intersection, Some[Vector] with the point where they intersect
   */
  def intersects(that: Line) = {
    val d1 = end - start
    val d2 = that.end - that.start
    val d = (d2.y * d1.x) - (d2.x * d1.y)

    if (d == 0) None
    else {
      val f1 = (start.x * end.y - start.y * end.x)
      val f2 = (that.start.x * that.end.y - that.start.y * that.end.x)
      val vx = f1 * (that.start.x - that.end.x) - (start.x - end.x) * f2
      val vy = f1 * (that.start.y - that.end.y) - (start.y - end.y) * f2
      Some(Vector(vx / d, vy / d))
    }
  }
}

case class Circle(center: Vector, radius: Float) extends Shape {
  override def area = scala.math.Pi.toFloat * radius * radius

  def transposed(v: Vector) = Circle(center + v, radius)

  def bounds = AABox(center.x - radius, center.y - radius, radius * 2, radius * 2)

  def intersects(that: Circle) = (that.center - this.center).lengthSqr < (radius * radius + that.radius * that.radius)
}

case class Quad(p1: Vector, p2: Vector, p3: Vector, p4: Vector) extends Shape {
  def transposed(v: Vector) = Quad(p1 + v, p2 + v, p3 + v, p4 + v)

  def bounds = AABox(min_x, min_y, max_x - min_x, max_y - min_y)

  private lazy val (min_x, max_x) = {
    val tmp = Seq(p1.x, p2.x, p3.x, p4.x)
    (tmp.min, tmp.max)
  }

  private lazy val (min_y, max_y) = {
    val tmp = Seq(p1.y, p2.y, p3.y, p4.y)
    (tmp.min, tmp.max)
  }

  val min = (min_x, min_y)
  val max = (min_x, min_y)
}