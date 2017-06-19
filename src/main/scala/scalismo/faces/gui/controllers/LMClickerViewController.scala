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

package scalismo.faces.gui.controllers

import java.io.File
import javax.imageio.ImageIO

import scalismo.faces.gui.models.LandmarksModel
import scalismo.faces.gui.views.{LMClickerView, LMClickerViewSwing}
import scalismo.faces.color.RGB
import scalismo.faces.image.{BufferedImageConverter, PixelImage, PixelImageDomain}
import scalismo.faces.io.PixelImageIO
import scalismo.geometry.{Point, _2D}

import scala.util.Try

/**
 * Landmarks clicker
 */
class LMClickerViewController(private var workingDirectory: File, private var files: Seq[File]) {
  import LMClickerViewController._

  require(workingDirectory.exists() && workingDirectory.isDirectory, "invalid working directory")

  val imageDomain = PixelImageDomain(1024, 1024)
  private var currentImage = PixelImage.view(imageDomain, (x, y) => RGB.White)

  val landmarksModel = LandmarksModel(imageDomain)
  val frameIcon = LandmarksModel.getLMIcon("clicker-icon")

  val clickerView: LMClickerView = LMClickerViewSwing(getLMWithIcons, workingDirectory.getPath, currentImage, frameIcon)

  var clickedIdsIterator = files match {
    case Nil => Iterator(listUnclickedImages (workingDirectory) ++ listClickedImages (workingDirectory) ).flatten
    case files => checkForFilesToBeClicked(workingDirectory, files)
  }

  setImageToNextFile() // load first image in directory, starts with unclicked

  clickerView.onResetButtonClick(() => if (landmarksModel.clickingEnabled) {
    resetLandmarks()
    updateView()
  })

  clickerView.onSaveButtonClick( () => if (landmarksModel.clickingEnabled) {
      saveLandmarksFile()
  })

  clickerView.onImageChosen(imageFile => {
    saveLandmarksFile() // save current clicking state
    openImage(imageFile)
  })

  clickerView.onLandmarkSetChosen(lmFile => {
    landmarksModel.readLandmarkSetFromFile(lmFile)
    clickerView.setLandmarkButtons(getLMWithIcons)
    landmarksModel.reset()

    clickerView.onLMButtonClick(btnText => {
      landmarksModel.setCurrent(btnText)
      updateView()
    })

    updateView()
  })

  clickerView.onLMButtonClick(btnText => {
    landmarksModel.setCurrent(btnText)
    updateView()
  })

  clickerView.onLoadNextIdButtonClick(() => {
    saveLandmarksFile()
    setImageToNextFile()
  })

  clickerView.onImageClick(
    actionLeftClick = (point: Point[_2D]) => {
    if (landmarksModel.clickingEnabled) {
      landmarksModel.add(point)
      landmarksModel.next
      updateView()
    }
  },
    actionRightClick = (point: Point[_2D]) => {
    if (landmarksModel.clickingEnabled) {
      val landmark = landmarksModel.getLandmarkByPosition(point)
      if (landmark.nonEmpty) {
        landmarksModel.remove(landmark.get.id)
        landmarksModel.setCurrent(landmark.get.id)
        updateView()
      }
    }
  }
  )

  clickerView.onNextButtonClick(() => {
    if (landmarksModel.clickingEnabled) {
      landmarksModel.next
      updateView()
    }
  })

  clickerView.onPreviousButtonClick(() => {
    if (landmarksModel.clickingEnabled) {
      landmarksModel.prev match {
        case Some(id: String) => landmarksModel.setCurrent(id)
        case _ => ()
      }
      updateView()
    }
  })

  clickerView.onToggleClick(b => {
    if (landmarksModel.clickingEnabled) landmarksModel.setVisible(b)
  })

  def getLMWithIcons = {
    landmarksModel.getLMLabels.map(l => {
      val lmIcon = LandmarksModel.getLMIcon(l)
      (l, lmIcon)
    })
  }

  def setImageToNextFile() = {

    val nextId = getNextImageFile
    if (nextId.isEmpty) {
      clickerView.showAlert("No Images/ID available")
    } else {
      openImage(nextId.get)
    }

  }

  def resetLandmarks() = landmarksModel.reset()

  def saveLandmarksFile() = {
    clickerView.getCurrentLandmarksFile match {
      case Some(file) => landmarksModel.saveLandmarksToFile(file)
      case None =>
    }
  }

  def updateHelpImage() = {
    val newHelpImage = getHelpImage(landmarksModel.current)
    newHelpImage.foreach(clickerView.updateHelpImage)
  }

  def resetButtons(): Unit = {
    landmarksModel.getLMLabels.foreach(id => {
      clickerView.resetButtonById(id)
    })
  }

  def updateButtonColor() = {
    resetButtons()
    landmarksModel.getAddedLandmarks.foreach(id => {
      clickerView.setButtonProcessed(id.id)
    })
    clickerView.setButtonActive(landmarksModel.current)
  }

  def updateImageCursor() = {
    clickerView.updateImageCursor(LandmarksModel.getLMIcon(landmarksModel.current))
  }

  def updateImage(): Unit = {
    clickerView.updateFaceImage(currentImage)
    updateView()
  }

  def updateView(): Unit = {
    clickerView.updateLandmarks(landmarksModel.getAddedLandmarks)
    updateButtonColor()
    updateHelpImage()
    updateImageCursor()
  }

  def openImage(imageFile: File) = {
    require(imageFile.exists(), "file does not exist: " + imageFile)

    updateWorkingDirectory(imageFile.getParentFile)

    val imageName = imageFile.getName
    val image = PixelImageIO.read[RGB](imageFile).get
    val lmFile = correspondingLandmarksFile(imageFile)

    resetLandmarks()

    currentImage = image

    landmarksModel.startClicking()
    landmarksModel.addLandmarksFromExistingFile(lmFile)

    clickerView.enableButtons()
    clickerView.updateTitle(imageName)
    clickerView.updateSavePath(lmFile)
    clickerView.updateFaceImage(currentImage)
    clickerView.updateLandmarks(landmarksModel.getAddedLandmarks)
    clickerView.packView()
    updateImage()
  }

  def getNextImageFile: Option[File] = {
    if (clickedIdsIterator.hasNext) Some(clickedIdsIterator.next()) else None
  }

  def updateWorkingDirectory(newDirectory: File): Unit = {
    require(newDirectory.exists() && newDirectory.isDirectory, s"invalid working directory: $newDirectory")
    if (newDirectory != workingDirectory) {
      workingDirectory = newDirectory
      clickedIdsIterator = Iterator.continually(listUnclickedImages(workingDirectory) ++ listClickedImages(workingDirectory)).flatten // Kind of circular iterator
      clickerView.updateWorkingDirectory(workingDirectory)
    }
  }
}

object LMClickerViewController {
  def apply(workingDirectory: File, imageFiles: Seq[File] = Nil) = {
    val clicker = new LMClickerViewController(workingDirectory, imageFiles)
  }

  private val fileSeparator: String = File.pathSeparator

  /** load a help image from the jar resources, heuristic file name matcher to find best help image */
  def getHelpImage(lm: String): Try[PixelImage[RGB]] = Try {
    val imageStream = getClass.getResourceAsStream("/help/"+lm+".png")
    val image = ImageIO.read(imageStream)
    BufferedImageConverter.toPixelImage(image)
  }

  /** filter image files only */
  def isImageFile(file: File): Boolean = file.isFile && (file.getName.toLowerCase.endsWith(".png") || file.getName.toLowerCase.endsWith(".jpg"))

  /** list all images in a directory */
  def listImageFilesInDirectory(directory: File): Seq[File] = {
    directory.listFiles.filter(isImageFile).sortBy { _.getName }
  }

  /** list all unclicked files in a directory */
  def listUnclickedImages(directory: File): Seq[File] = {
    listImageFilesInDirectory(directory).filter { !correspondingLandmarksFile(_).exists() }
  }

  /** list all clicked files in a directory */
  def listClickedImages(directory: File): Seq[File] = {
    listImageFilesInDirectory(directory).filter { correspondingLandmarksFile(_).exists() }
  }

  /** Check which files can be found that are passed. */
  def checkForFilesToBeClicked(workingDirectory: File, files: Seq[File]): Iterator[File] = {
    files.flatMap { file =>
      if (file.isAbsolute) {
        println("one "+file.toString())
        Some(file)
      }
      else if (file.exists()) {
        println("two "+file.toString)
        Some(file)
      }
      else {
        val lastChance = new File(workingDirectory, file.toString)
        if (lastChance.exists()) {
          println("three "+lastChance.toString)
          Some(lastChance)
        }
        else {
          println(s"Warning: Could not find the image file for: ${file.toString}")
          None
        }
      }
    }.iterator
  }

  /** corresponding landmarks file to a given image file */
//  def correspondingLandmarksFile(imageFile: File, faceNumber: Int = 0) = {
//    val fileName = imageFile.getName.substring(0, imageFile.getName.lastIndexOf("."))
//    new File(imageFile.getParent, fileName + s"_face$faceNumber.tlms")
//  }

// removed _face[IDX] from name as default.
  def correspondingLandmarksFile(imageFile: File) = {
    val fileName = imageFile.getName.substring(0, imageFile.getName.lastIndexOf("."))
    new File(imageFile.getParent, fileName + ".tlms")
  }
}