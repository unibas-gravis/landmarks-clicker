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

import scalismo.faces.color.RGB
import scalismo.faces.image.PixelImage
import scalismo.faces.landmarks.TLMSLandmark2D
import scalismo.geometry.{Point, _2D}


trait LMClickerView {
  def setButtonActive(id: String)

  def setButtonProcessed(id: String)

  def enableButtons()

  def updateFaceImage(image: PixelImage[RGB])

  def updateLandmarks(landmarkSeq: Seq[TLMSLandmark2D])

  def setLandmarkButtons(buttonLabels: Seq[(String, BufferedImage)]): Unit

  def updateSavePath(lmFile: File)

  def updateHelpImage(newHelpImage: PixelImage[RGB])

  def updateTitle(imageName: String)

  def updateWorkingDirectory(directory: File): Unit

  def resetButtonById(id: String)

  def onImageChosen(action: (File) => Unit)

  def onLandmarkSetChosen(action: (File) => Unit)

  def onLMButtonClick(action: (String) => Unit)

  def onLoadNextIdButtonClick(action: () => Unit)

  def onResetButtonClick(action: () => Unit)

  def onSaveButtonClick(action: () => Unit)

  def onNextButtonClick(action: () => Unit)

  def onPreviousButtonClick(action: () => Unit)

  def onToggleClick(action: (Boolean) => Unit)

  def onImageClick(actionLeftClick: (Point[_2D]) => Unit, actionRightClick: (Point[_2D]) => Unit)

  def onImageResize(action: () => Unit)

  def updateImageCursor(icon: BufferedImage)

  def getCurrentLandmarksFile: Option[File]

  def getImageWidth: Int

  def getImageHeight: Int

  def packView()

  def showAlert(message: String)
}
