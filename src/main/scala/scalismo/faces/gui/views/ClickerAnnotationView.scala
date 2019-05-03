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
package scalismo.faces.gui.views

import java.awt._
import javax.swing.{GrayFilter, JComponent, JPanel, SwingUtilities}
import javax.swing.plaf.LayerUI

import scalismo.color.RGB
import scalismo.faces.gui.models.LandmarksModel
import scalismo.faces.landmarks.TLMSLandmark2D
import scalismo.geometry.{Point, _2D}
import java.awt
import java.awt.event.{MouseEvent, MouseListener}

class ClickerAnnotationView(val color: RGB, val correctCoordinate: (Point[_2D]) => awt.Point) extends LayerUI[JPanel] {

  var landmarks: Seq[TLMSLandmark2D] = Seq()

  def update(tlmsLandmarks2D: Seq[TLMSLandmark2D]): Unit = {
    landmarks = tlmsLandmarks2D
  }

  override def paint(g: Graphics, c: JComponent): Unit = {
    super.paint(g, c)

    val g2d = g.create().asInstanceOf[Graphics2D]
    val w = c.getWidth
    val h = c.getHeight

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setPaint(new Color(color.r.toFloat, color.g.toFloat, color.b.toFloat))

    for (lm <- landmarks){
      val lmImage = LandmarksModel.getLMIcon(lm.id)

      val posInWindow = correctCoordinate(lm.point)

      val center = Point(
        (posInWindow.x - lmImage.getWidth / 2),
        (posInWindow.y - lmImage.getHeight / 2)
      )

      if(lm.visible) {
        g2d.drawImage(lmImage, center.x.toInt, center.y.toInt, null)
      } else {
        g2d.drawImage(GrayFilter.createDisabledImage(lmImage), center.x.toInt, center.y.toInt, null)
      }

    }
    g2d.dispose()
  }

}
