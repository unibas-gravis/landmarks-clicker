/*
 * Copyright University of Basel, Graphics and Vision Research Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scalismo.faces.gui.ext

import java.awt
import java.awt.event._
import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Graphics}

import breeze.linalg.{max, min}
import breeze.numerics.pow
import scalismo.color.RGB
import scalismo.faces.image.{BufferedImageConverter, PixelImage}
import scalismo.geometry.{EuclideanVector, Point, _2D}

/**
  * Image panel that can move and zoom images.
  *
  */
class ImageZoomPanel(width: Int, height: Int, image: PixelImage[RGB])
  extends ImagePanel(width, height, image) with MouseListener with MouseMotionListener with MouseWheelListener {
  setPreferredSize(new Dimension(width, height))

  this.addMouseListener(this)
  this.addMouseMotionListener(this)
  this.addMouseWheelListener(this)


  object ViewerState extends Enumeration {
    type ViewerState = Value
    val NEUTRAL, ZOOM, MOVE = Value
  }
  import ViewerState._

  var bufferedImage: BufferedImage = BufferedImageConverter.toBufferedImage(image)

  val minZoom = 0.05
  val maxZoom = 4.0
  var downPosition: Point[_2D] = Point(-1, -1)
  var zoom: Double = initialZoom()
  var Tx = initialTx()
  var txD: Int = 0
  var Ty = initialTy()
  var tyD: Int = 0
  var state: ViewerState = NEUTRAL

  def initialZoom() = max(minZoom,min(maxZoom,min(width.toDouble/bufferedImage.getWidth,height.toDouble/bufferedImage.getHeight)))
  def initialTx() = ((width - bufferedImage.getWidth*zoom) / 2).toInt
  def initialTy() = ((height - bufferedImage.getHeight*zoom) / 2).toInt

  override def updateImage(image: PixelImage[RGB]): Unit = {
    super.updateImage(image)
    setPreferredSize(new Dimension(width, height))
    downPosition = Point(-1, -1)
    zoom = initialZoom()
    Tx = initialTx()
    txD = 0
    Ty = initialTy()
    tyD = 0
    state = NEUTRAL
    bufferedImage = BufferedImageConverter.toBufferedImage(image)
    repaint()
  }

  override def paintComponent(g: Graphics): Unit = {
    g.setColor(new Color(1.0f,1.0f,1.0f,1.0f))
    g.clearRect(0,0,this.width,this.height)
    val scaling = zoom
    val newW = (bufferedImage.getWidth * scaling).toInt
    val newH = (bufferedImage.getHeight * scaling).toInt
    val scaledImage = bufferedImage.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH)
    g.drawImage(scaledImage, Tx + txD, Ty + tyD, new Color(0, 0, 0), null)
  }


  override def mouseExited(e: MouseEvent): Unit = {}

  override def mouseClicked(e: MouseEvent): Unit = {}

  override def mouseEntered(e: MouseEvent): Unit = {}

  override def mousePressed(e: MouseEvent): Unit = {
    downPosition = Point(e.getX, e.getY)

    if (e.getButton == MouseEvent.BUTTON3 && state == NEUTRAL) {
      state = MOVE
      txD = 0
      tyD = 0
    }

    repaint()
  }

  override def mouseWheelMoved(e: MouseWheelEvent): Unit = {

    if ( e.isControlDown ) {
      val mousePos = Point(e.getX, e.getY)
      val diff = e.getPreciseWheelRotation

      val zoomD = if (diff > 0) {
        min(max(pow(1.05, diff), minZoom / zoom), maxZoom / zoom)
      } else if (diff < 0) {
        min(max(pow(0.95, -diff), minZoom / zoom), maxZoom / zoom)
      } else {
        1.0
      }
      val dT = mousePos.toVector * (1.0 - zoomD) + EuclideanVector(Tx, Ty) * (zoomD - 1.0)

      Tx += dT.x.round.toInt
      Ty += dT.y.round.toInt
      zoom *= zoomD
    } else if (e.isShiftDown) {
      val horizontalScroll = e.getPreciseWheelRotation
      Tx -= (2 * horizontalScroll).toInt
    } else {
      val verticalScroll = e.getPreciseWheelRotation
      Ty -= (2 * verticalScroll).toInt
    }

    repaint()
  }

  override def mouseReleased(e: MouseEvent): Unit = {

    if (e.getButton == MouseEvent.BUTTON3 && state == MOVE) {
      Tx += txD
      Ty += tyD
      txD = 0
      tyD = 0
    }

    state = NEUTRAL
    downPosition = Point(-1, -1)
    repaint()
  }

  override def mouseMoved(e: MouseEvent): Unit = {}

  override def mouseDragged(e: MouseEvent): Unit = {
    val p = Point(e.getX, e.getY)

    if (state == MOVE) {
      txD = -(downPosition.x - p.x).toInt
      tyD = -(downPosition.y-p.y).toInt
    }

    repaint()
  }

  override def screenToImage(screenPoint: awt.Point): Point[_2D] = {
    val newPos = Point(screenPoint.x,screenPoint.y)
    val offset = EuclideanVector(Tx+txD,Ty+tyD)
    val imagePoint = ((newPos.toVector-offset)/(zoom)).toPoint
    imagePoint
  }

  override def imageToScreen(imagePoint: Point[_2D]): awt.Point = {
    val offset = EuclideanVector(Tx+txD,Ty+tyD)
    val newPos = (imagePoint.toVector*(zoom)+offset).toPoint
    new awt.Point(newPos.x.toInt,newPos.y.toInt)
  }
}
