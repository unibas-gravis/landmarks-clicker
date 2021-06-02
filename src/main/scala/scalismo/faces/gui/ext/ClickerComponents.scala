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

import scalismo.color.RGB
import scalismo.faces.image.PixelImage
import scalismo.geometry
import scalismo.geometry._2D

import java.awt._
import java.awt.event._

class ClickerComponents(var backgroundImage: PixelImage[RGB], var mousePosition: java.awt.Point = new java.awt.Point(0, 0))
    extends ImageZoomPanel(backgroundImage.width, backgroundImage.height, backgroundImage) {

  addMouseMotionListener(new MouseAdapter {
    override def mouseMoved(e: MouseEvent): Unit = {
      mousePosition = e.getPoint
      repaint()
    }
  })

  override def onImageClick(actionLeftClick: geometry.Point[_2D] => Unit, actionRightClick: geometry.Point[_2D] => Unit): Unit = {
    super.onImageClick(actionLeftClick, actionRightClick)
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    if (isMouseOnPanel) {
      g.drawLine(0, mousePosition.getY.toInt, this.getWidth, mousePosition.getY.toInt)
      g.drawLine(mousePosition.getX.toInt, 0, mousePosition.getX.toInt, this.getHeight)
    }
  }

  def isMouseOnPanel: Boolean = {
    val mousePos = MouseInfo.getPointerInfo.getLocation
    val bounds = getBounds()
    bounds.setLocation(getLocationOnScreen)
    bounds.contains(mousePos)
  }

  def onResize(action: () => Unit): Unit = {
    this.addComponentListener(new ComponentAdapter() {
      override def componentResized(e: ComponentEvent): Unit = { action() }
    })
  }
}
