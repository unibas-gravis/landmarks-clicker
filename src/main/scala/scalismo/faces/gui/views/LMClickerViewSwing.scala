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
import java.awt.event._
import java.awt.image.BufferedImage
import java.io.File
import javax.swing._

import scalismo.faces.gui.ext.GUIBlock._
import scalismo.faces.gui.ext.{ClickerComponents, ImagePanel, LandmarkButton}
import scalismo.color.RGB
import scalismo.faces.gui.GUIFrame
import scalismo.faces.image.PixelImage
import scalismo.faces.landmarks.TLMSLandmark2D
import scalismo.faces.utils.LanguageUtilities
import scalismo.geometry.{Point, _2D}

/**
  * Landmarks Clicker View for the scala swing framework. Implements the general [[LMClickerView]] interface.
  */
class LMClickerViewSwing(buttonLabels: IndexedSeq[(String, BufferedImage)], workingDirectory: String, startImage: PixelImage[RGB], frameIcon: BufferedImage)
    extends LMClickerView {
  val viewTitle: String = "Landmarks Clicker"

  private val imagePanel = new ClickerComponents(startImage)
  private val annotationLayer = new ClickerAnnotationView(RGB(1.0, 0, 0), imagePanel.imageToScreen)
  private val imageView = new JLayer[JPanel](imagePanel, annotationLayer)

  private val helpImage = PixelImage.view(500, 800, (_, _) => RGB(1, 1, 1))

  private val helpImageView = new ImagePanel(500, 800, helpImage)

  private val imageChooser = chooser(workingDirectory)

  private val outputPathEditText = textBox(60, "")
  private val chooseImageButton = button("Choose Image/ID")
  private val loadNextIDButton = button("Load next ID")

  private val resetButton = button("Reset Landmarks", enabled = false)
  private val nextButton = button("Next", enabled = false)
  private val prevButton = button("Previous", enabled = false)
  private val saveButton = button("Save", enabled = false)

  private val visibilityToggle = toggleButton("LM Visible", selected = true)

  private val workingDirectoryTextBox = textBox(workingDirectory.length, workingDirectory)
  workingDirectoryTextBox.setEditable(false)

  private val landmarkChooser = lmsChooser(workingDirectory)
  private val chooseLandmarkSetButton = button("Choose LM-Set")

  private val currentImageTextBox = textBox(20, "")
  currentImageTextBox.setEditable(false)

  private var lmButtons = updateLandmarksButtons(buttonLabels)
  def updateLandmarksButtons(buttonLabels: Seq[(String, BufferedImage)]): Seq[LandmarkButton] = {
    buttonLabels.map(l => {
      LanguageUtilities.withMutable(LandmarkButton(l._1)) { btn =>
        btn.setEnabled(false)
        btn.setHorizontalAlignment(SwingConstants.LEFT)
        btn.setIcon(new ImageIcon(l._2))
      }
    })
  }
  override def setLandmarkButtons(buttonLabels: Seq[(String, BufferedImage)]): Unit = {
    lmButtons = updateLandmarksButtons(buttonLabels)
    updateLMButtonsUI()
  }

  override def resetButtonById(id: String): Unit = changeButtonState(id, btn => btn.resetLayout())

  override def setButtonActive(id: String): Unit = changeButtonState(id, btn => btn.setActive())
  override def setButtonProcessed(id: String): Unit = changeButtonState(id, btn => btn.setProcessed())
  def changeButtonState(id: String, doAction: LandmarkButton => Unit): Unit = {
    val btn = lmButtons.find(_.text == id)
    if (btn.isDefined) {
      doAction(btn.get)
    }
  }

  override def showAlert(message: String): Unit = {
    alert(message)
  }

  override def enableButtons(): Unit = {
    resetButton.setEnabled(true)
    visibilityToggle.setEnabled(true)
    nextButton.setEnabled(true)
    prevButton.setEnabled(true)
    lmButtons.foreach(_.setEnabled(true))
    saveButton.setEnabled(true)
  }

  override def updateFaceImage(image: PixelImage[RGB]): Unit = {
    imagePanel.updateImage(image)
  }

  override def updateLandmarks(lmSeq: Seq[TLMSLandmark2D]): Unit = {
    annotationLayer.update(lmSeq)
  }

  override def updateSavePath(lmFile: File): Unit = outputPathEditText.setText(lmFile.getAbsolutePath)

  chooseImageButton.addActionWithShortcut(() => imageChooser.showOpenDialog(imageView), Some("control O"))
  chooseLandmarkSetButton.addActionWithShortcut(() => landmarkChooser.showOpenDialog(imageView), Some("control L"))
  override def onLMButtonClick(action: String => Unit): Unit = lmButtons.foreach(btn => btn.onClick(_ => action(btn.getText)))
  override def onLoadNextIdButtonClick(action: () => Unit): Unit = loadNextIDButton.addActionWithShortcut(action, Some("control N"))
  override def onResetButtonClick(action: () => Unit): Unit = resetButton.addActionWithShortcut(action, Some("control D"))
  override def onSaveButtonClick(action: () => Unit): Unit = saveButton.addActionWithShortcut(action, Some("control S"))
  override def onNextButtonClick(action: () => Unit): Unit = nextButton.addActionWithShortcut(action, Some("N"))
  override def onPreviousButtonClick(action: () => Unit): Unit = prevButton.addActionWithShortcut(action, Some("P"))

  override def onToggleClick(action: Boolean => Unit): Unit =
    visibilityToggle.addItemListener((e: ItemEvent) => action(e.getStateChange == ItemEvent.SELECTED))

  override def onImageChosen(action: File => Unit): Unit = {
    imageChooser.onImageChosen { _ =>
      Option(imageChooser.getSelectedFile).foreach(action)
    }
  }

  override def onLandmarkSetChosen(action: File => Unit): Unit = {
    landmarkChooser.onLandmarkSetChosen { _ =>
      Option(landmarkChooser.getSelectedFile).foreach(action)
    }
  }

  override def updateHelpImage(newHelpImage: PixelImage[RGB]): Unit = {
    helpImageView.updateImage(newHelpImage)
  }

  override def updateTitle(title: String): Unit = {
    frame.setTitle(s"$viewTitle - $title")
    currentImageTextBox.setText(title)
  }

  override def onImageClick(actionLeftClick: Point[_2D] => Unit, actionRightClick: Point[_2D] => Unit): Unit = {
    imagePanel.onImageClick(actionLeftClick, actionRightClick)
  }

  override def updateImageCursor(icon: BufferedImage): Unit = {
    val toolkit = Toolkit.getDefaultToolkit
    val dim = toolkit.getBestCursorSize(icon.getWidth, icon.getHeight)
    val centerX = dim.width / 2 // Take cursor icon-size into account
    val centerY = dim.height / 2
    val c = toolkit.createCustomCursor(icon, new java.awt.Point(centerX, centerY), "img")
    imageView.setCursor(c) //.changeCursorOnHover(i  con)
  }

  override def getCurrentLandmarksFile: Option[File] = {
    val outputFile = outputPathEditText.getText
    if (outputFile.isEmpty)
      None
    else
      Some(new File(outputFile))
  }

  private val lmButtonsStack: JPanel = new JPanel(); updateLMButtonsUI()
  def updateLMButtonsUI(): Unit = {
    lmButtonsStack.removeAll()
    lmButtonsStack.add(fullWidthStack(lmButtons: _*))
  }

  private val frame: GUIFrame = {
    verticalSplitter(
      shelf(
        chooseImageButton,
        workingDirectoryTextBox,
        currentImageTextBox,
        loadNextIDButton
      ),
      horizontalSplitter(
        fullWidthStack(chooseLandmarkSetButton, lmButtonsStack),
        LanguageUtilities.withMutable(
          verticalSplitter(
            LanguageUtilities.withMutable(
              horizontalSplitter(
                imageView,
                stack(
                  LanguageUtilities.withMutable(textBox(20, "Help images")) { t =>
                    t.setEditable(false)
                  },
                  helpImageView
                )
              )) { _.setResizeWeight(1.0) },
            shelf(
              visibilityToggle,
              prevButton,
              nextButton,
              resetButton,
              outputPathEditText,
              saveButton
            )
          )) { _.setResizeWeight(1.0) }
      )
    ).displayInNewFrame(viewTitle)
  }

  frame.setIconImage(frameIcon)

  override def packView(): Unit = frame.pack()

  override def getImageWidth: Int = imageView.getWidth

  override def getImageHeight: Int = imageView.getHeight

  override def onImageResize(action: () => Unit): Unit = { imagePanel.onResize(action) }

  override def updateWorkingDirectory(directory: File): Unit = {
    workingDirectoryTextBox.setText(directory.getAbsolutePath)
    workingDirectoryTextBox.setColumns(directory.getAbsolutePath.length)
    imageChooser.setCurrentDirectory(directory)
  }

}

object LMClickerViewSwing {
  def apply(
      labels: IndexedSeq[(String, BufferedImage)],
      stdDir: String,
      startImage: PixelImage[RGB],
      frameIcon: BufferedImage
  ) = new LMClickerViewSwing(labels, stdDir, startImage, frameIcon)
}
