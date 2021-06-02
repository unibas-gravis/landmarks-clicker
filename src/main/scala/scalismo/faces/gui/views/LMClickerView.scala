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

import java.awt.image.BufferedImage
import java.io.File

import scalismo.color.RGB
import scalismo.faces.image.PixelImage
import scalismo.faces.landmarks.TLMSLandmark2D
import scalismo.geometry.{Point, _2D}

trait LMClickerView {
  def setButtonActive(id: String): Unit

  def setButtonProcessed(id: String): Unit

  def enableButtons(): Unit

  def updateFaceImage(image: PixelImage[RGB]): Unit

  def updateLandmarks(landmarkSeq: Seq[TLMSLandmark2D]): Unit

  def setLandmarkButtons(buttonLabels: Seq[(String, BufferedImage)]): Unit

  def updateSavePath(lmFile: File): Unit

  def updateHelpImage(newHelpImage: PixelImage[RGB]): Unit

  def updateTitle(imageName: String): Unit

  def updateWorkingDirectory(directory: File): Unit

  def resetButtonById(id: String): Unit

  def onImageChosen(action: File => Unit): Unit

  def onLandmarkSetChosen(action: File => Unit): Unit

  def onLMButtonClick(action: String => Unit): Unit

  def onLoadNextIdButtonClick(action: () => Unit): Unit

  def onResetButtonClick(action: () => Unit): Unit

  def onSaveButtonClick(action: () => Unit): Unit

  def onNextButtonClick(action: () => Unit): Unit

  def onPreviousButtonClick(action: () => Unit): Unit

  def onToggleClick(action: Boolean => Unit): Unit

  def onImageClick(actionLeftClick: Point[_2D] => Unit, actionRightClick: Point[_2D] => Unit): Unit

  def onImageResize(action: () => Unit): Unit

  def updateImageCursor(icon: BufferedImage): Unit

  def getCurrentLandmarksFile: Option[File]

  def getImageWidth: Int

  def getImageHeight: Int

  def packView(): Unit

  def showAlert(message: String): Unit
}
