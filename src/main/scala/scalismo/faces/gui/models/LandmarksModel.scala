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

package scalismo.faces.gui.models

import java.awt.image.BufferedImage
import java.awt.{Color, RenderingHints}
import java.io.File
import javax.imageio.ImageIO
import javax.swing.GrayFilter
import breeze.linalg.functions.euclideanDistance
import scalismo.color.RGBA
import scalismo.faces.image.{BufferedImageConverter, PixelImage, PixelImageDomain}
import scalismo.faces.landmarks.TLMSLandmark2D
import scalismo.faces.io.TLMSLandmarksIO
import scalismo.geometry.{Point, _2D}

import scala.collection.mutable
import scala.util.Try

object LandmarksModel {
  def apply(domain: PixelImageDomain) = new LandmarksModel(domain)

  val lmMap: mutable.Map[String, BufferedImage] = mutable.Map[String, BufferedImage]()

  /** load a landmarks icon from resources in the jar file */
  def getLMIcon(lm: String): BufferedImage = {
    if (lmMap.contains(lm)) return lmMap(lm)

    val resource = getClass.getResource(s"/lmicons/${lm}_thumb.png")
    val image = if (resource != null) {
      ImageIO.read(resource.openStream())
    } else {
      val defaultResource = getClass.getResource(s"/lmicons/default_thumb.png")
      if (defaultResource != null) {
        ImageIO.read(defaultResource.openStream())
      } else {
        getRedDot
      }
    }
    lmMap += Tuple2(lm, image)
    image
  }

  def getRedDot: BufferedImage = {
    val dot = new BufferedImage(25, 25, BufferedImage.TYPE_INT_ARGB)
    val g2d = dot.createGraphics()
    g2d.setColor(new Color(255, 0, 0, 80))
    g2d.fillOval(7, 7, 9, 9)
    g2d.dispose()
    dot
  }
}

class LandmarksModel(domain: PixelImageDomain) {
  def readLandmarkSetFromFile(file: File): Unit = {
    val tlms = TLMSLandmarksIO.read2D(file).get
    landmarks = tlms.map(tlms => tlms.id)
    tlms.foreach(l => landmarksMap.update(l.id, None))
  }

  object State extends Enumeration {
    type State = Value
    val Start, NormalClicking, NotVisible = Value
  }
  import State._

  private var state = Start
  private var landmarks = IndexedSeq(
    "right.eye.corner_outer",
    "right.eye.corner_inner",
    "left.eye.corner_inner",
    "left.eye.corner_outer",
    "right.nose.wing.tip",
    "center.nose.tip",
    "left.nose.wing.tip",
    "right.lips.corner",
    "center.lips.upper.outer",
    "center.lips.lower.outer",
    "left.lips.corner",
    "center.chin.tip"
  )

  private var currentLandmark = 0
  private var prevLandmark = -1
  private var wasLastClicked = false
  val landmarksMap: mutable.Map[String, Option[TLMSLandmark2D]] = mutable.Map(landmarks.zip(List.fill(landmarks.size)(None: Option[TLMSLandmark2D])): _*)

  def clickingEnabled: Boolean = state != Start

  def startClicking(): Unit = {
    state = NormalClicking
  }

  def setVisible(b: Boolean): Unit = {
    if (b)
      state = NormalClicking
    else
      state = NotVisible
  }

  def getLandmarkByPosition(point: scalismo.geometry.Point[_2D], threshold: Double = 25d): Option[TLMSLandmark2D] = {
    val distTuple = landmarksMap.values.flatten.foldLeft((None: Option[TLMSLandmark2D], Double.MaxValue))((prevDist, currentLM: TLMSLandmark2D) => {
      val distance = euclideanDistance(point.toBreezeVector, currentLM.point.toBreezeVector)
      if (prevDist._2 > distance) (Some(currentLM), distance) else prevDist
    })
    if (distTuple._2 < threshold) distTuple._1 else None
  }

  def setCurrent(id: String): Unit = {
    prevLandmark = currentLandmark
    currentLandmark = landmarks.indexOf(id)
    wasLastClicked = false
  }

  def getLMLabels: Seq[String] = landmarks

  def current: String = landmarks(currentLandmark)

  def prev: Option[String] = landmarks.lift(currentLandmark - 1)

  def next: Option[String] = {
    if (state != Start) {
      prevLandmark = currentLandmark
      if (currentLandmark < landmarks.size - 1) {
        currentLandmark += 1
        Some(landmarks(currentLandmark))
      } else None
    } else None
  }

  def remove(landmarkID: String): Option[Option[TLMSLandmark2D]] = landmarksMap.remove(landmarkID)

  def add(point: Point[_2D]): Unit = {
    wasLastClicked = true
    val id = current
    if (state == NormalClicking) {
      landmarksMap.update(id, Some(TLMSLandmark2D(id, point, visible = true)))
    } else if (state == NotVisible) {
      landmarksMap.update(id, Some(TLMSLandmark2D(id, point, visible = false)))
    }
  }

  def addLandmarksFromExistingFile(lmFile: File): Unit = {
    if (lmFile.exists()) {
      for (current <- TLMSLandmarksIO.read2D(lmFile).get) {
        landmarksMap.update(current.id, Some(current.copy(point = current.point)))
      }
    }
  }

  def saveLandmarksToFile(tlmsFile: File): Try[Unit] = {
    val transformedCoords = getAddedLandmarks.map(lm => {
      lm.copy(point = lm.point)
    })

    TLMSLandmarksIO.write2D(transformedCoords, tlmsFile)
  }

  def getAddedLandmarks: IndexedSeq[TLMSLandmark2D] = {
    landmarks.flatMap(landmarksMap(_))
  }

  def reset(): Unit = {
    currentLandmark = 0
    prevLandmark = -1
    landmarksMap.foreach(m => {
      landmarksMap.update(m._1, None)
    })
  }

  /** draw landmarks into image with special icon for each landmark */
  def drawLandmarksWithCustomIcon(image: PixelImage[RGBA], landmarks: Iterable[TLMSLandmark2D], color: RGBA): PixelImage[RGBA] = {
    val bufImg: BufferedImage = BufferedImageConverter.toBufferedImage(image)
    val g2d = bufImg.createGraphics()
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setPaint(new Color(color.r.toFloat, color.g.toFloat, color.b.toFloat))
    for (lm <- landmarks) {

      val lmImage = LandmarksModel.getLMIcon(lm.id)
      if (lm.visible) {
        g2d.drawImage(lmImage, (lm.point.x - lmImage.getWidth / 2).toInt, (lm.point.y - lmImage.getHeight / 2).toInt, null)
      } else {
        g2d.drawImage(GrayFilter.createDisabledImage(lmImage), (lm.point.x - lmImage.getWidth / 2).toInt, (lm.point.y - lmImage.getHeight / 2).toInt, null)
      }

    }
    g2d.dispose()
    BufferedImageConverter.toPixelImage(bufImg)
  }
}
