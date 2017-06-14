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

import java.awt._
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import java.io.File
import javax.swing._
import javax.swing.event.{ChangeEvent, ChangeListener}
import javax.swing.filechooser.FileNameExtensionFilter

import scalismo.faces.color.RGB
import scalismo.faces.gui.GUIFrame
import scalismo.faces.image.{BufferedImageConverter, PixelImage}
import scalismo.faces.utils.LanguageUtilities
import scalismo.faces.utils.LanguageUtilities._
import scalismo.geometry.{Point, _2D}


object GUIBlock {
  /** implicit attachment of display functions to JComponents */
  implicit def pimpComponent(component: JComponent): GUIBlock = new GUIBlock(component)

  /** GUI block: can display itself in a GUIFrame */
  class GUIBlock(val component: JComponent) {
    def displayInNewFrame(title: String) = GUIFrame(title, component)

    def displayIn(frame: GUIFrame) = frame.display(component)
  }

  /** horizontal container */
  def shelf(components: JComponent*): JPanel = {
    withMutable(new JPanel()) { panel =>
      panel.setLayout(new FlowLayout())
      components.foreach(panel.add(_))
    }
  }

  /** vertical container, center aligned */
  def stack(components: JComponent*): Box = {
    withMutable(new Box(BoxLayout.Y_AXIS)) { b =>
      components.foreach { c =>
        c.setAlignmentX(Component.CENTER_ALIGNMENT)
        b.add(c)
      }
    }
  }

  def fullWidthStack(compenents: JComponent*): JPanel = {
    LanguageUtilities.withMutable(new JPanel) { panel =>
      panel.setLayout(new GridBagLayout())
      panel.setAlignmentX(SwingConstants.TOP)
      val gbc = new GridBagConstraints()
      gbc.weightx = 1
      gbc.weighty = 1
      gbc.fill = GridBagConstraints.HORIZONTAL
      gbc.gridwidth = GridBagConstraints.REMAINDER
      gbc.anchor = GridBagConstraints.NORTH
      compenents.foreach(panel.add(_, gbc))
    }
  }

  /** horizontal split pane */
  def horizontalSplitter(left: JComponent, right: JComponent): JSplitPane = {
    withMutable(new JSplitPane()) { p =>
      p.setOrientation(JSplitPane.HORIZONTAL_SPLIT)
      p.setLeftComponent(left)
      p.setRightComponent(right)
    }
  }

  /** vertical split pane */
  def verticalSplitter(top: JComponent, bottom: JComponent): JSplitPane = {
    withMutable(new JSplitPane()) { p =>
      p.setOrientation(JSplitPane.VERTICAL_SPLIT)
      p.setTopComponent(top)
      p.setBottomComponent(bottom)
    }
  }

  /** arrange components in a grid */
  def grid(cols: Int, rows: Int, component: => JComponent): JPanel = {
    withMutable(new JPanel()) { p =>
      p.setLayout(new GridLayout(cols, rows))
      for (x <- 0 until cols; y <- 0 until rows) p.add(component)
    }
  }

  def scroll(comp: JComponent) = new JScrollPane(comp)

  /** display a message */
  def alert(msg: String, parent: JComponent): Unit = javax.swing.JOptionPane.showMessageDialog(parent, msg)

  /** display a message */
  def alert(msg: String): Unit = alert(msg, null)

  def button(text: String, enabled: Boolean = true) = new JButton(text) {
    /**
      * Adds an action which can be called also by a shortcut
      *
      * @param action   action to be executed on click
      * @param shortcut e.g. "control S"
      */
    def addActionWithShortcut(action: () => Unit, shortcut: Option[String]): Unit = {
      addActionListener(
        LanguageUtilities.withMutable(new AbstractAction() {
          override def actionPerformed(e: ActionEvent): Unit = {
            action()
          }
        }) { l => {
          if (shortcut.isDefined) {
            l.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortcut.get))
            getActionMap.put(s"${shortcut}Action", l)
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(l.getValue(Action.ACCELERATOR_KEY).asInstanceOf[KeyStroke],
              s"${shortcut}Action")
            setToolTipText(s"Shortcut: ${shortcut.get}")
          }
        }
        })
    }

    setEnabled(enabled)
  }

  /** create a button with a listener */
  def button(text: String, clickListener: () => Unit): JButton = {
    withMutable(new JButton(text)) { b =>
      b.addActionListener(new ActionListener {
        override def actionPerformed(e: ActionEvent): Unit = clickListener()
      })
    }
  }

  /** create an file chooser for images */
  def chooser(stdDir: String): ImageChooser = new ImageChooser(stdDir)
  def lmsChooser(stdDir: String): LandmarkSetChooser = new LandmarkSetChooser(stdDir)

  def toggleButton(text: String, selected: Boolean) = new JToggleButton(text, selected)

  /** create a text box */
  def textBox(cols: Int, text: String) = {
    withMutable(new JTextField()) { t =>
      t.setColumns(cols)
      t.setText(text)
    }
  }

  /** create a simple label element */
  def label(text: String) = new JLabel(text)

  /** create a slider element */
  def slider(min: Int, max: Int, value: Int, changeListener: Int => Unit, orientation: Int = SwingConstants.VERTICAL): JSlider = {
    withMutable(new JSlider(orientation, min, max, value)) { s =>
      s.addChangeListener(new ChangeListener {
        override def stateChanged(e: ChangeEvent): Unit = changeListener(s.getValue)
      })
    }
  }

  /** draw a separator */
  def separator(orientation: Int = SwingConstants.VERTICAL) = new JSeparator(orientation)

  /** pad a component */
  def pad(component: JComponent) = withMutable(new JPanel()) { panel =>
    panel.setLayout(new GridLayout())
    panel.add(component)
  }
}

class ImageChooser(workingDirectory: String) extends JFileChooser {
  val filter = new FileNameExtensionFilter(
    "JPG & PNG Images", "jpg", "png")
  setFileFilter(filter)
  setCurrentDirectory(new File(workingDirectory))

  def onImageChosen(callback: (ActionEvent) => Unit): Unit = {
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        callback(e)
      }
    })
  }
}

class LandmarkSetChooser(workingDirectory: String) extends JFileChooser {
  val filter = new FileNameExtensionFilter(
    "TLMS files", "tlms")
  setFileFilter(filter)
  setCurrentDirectory(new File(workingDirectory))

  def onLandmarkSetChosen(callback: (ActionEvent) => Unit): Unit = {
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        callback(e)
      }
    })
  }
}

/** JPanel to display an image, the image is requested on each update */
class ImagePanel(width: Int, height: Int, var image: PixelImage[RGB]) extends JPanel {
  setPreferredSize(new Dimension(width, height))

  private def imageScale = {
    val sx = getWidth.toDouble / image.width
    val sy = getHeight.toDouble / image.height
    math.min(sx, sy)
  }

  private def imageOffset: (Int, Int) = {
    val scale = imageScale
    val dx = (getWidth - (image.width * scale).toInt) / 2
    val dy = (getHeight - (image.height * scale).toInt) / 2
    (dx, dy)
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    // find drawing image size, keep aspect ratio
    val scale = imageScale
    val (dx, dy) = imageOffset
    val bufferedImage: BufferedImage = BufferedImageConverter.toBufferedImage(image)
    g.drawImage(bufferedImage, dx, dy, (image.width * scale).toInt, (image.height * scale).toInt, null)
  }

  /** update the image (repaints this panel) */
  def updateImage(image: PixelImage[RGB]) = {
    this.image = image
    val dim = new Dimension(image.width, image.height)
    setPreferredSize(dim)
    repaint()
  }

  def onImageClick(actionLeftClick: (Point[_2D]) => Unit, actionRightClick: (Point[_2D]) => Unit) = {
    addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = {
        val imagePoint = screenToImage(e.getPoint)
        if (imagePoint.x >= 0 && imagePoint.x < image.width && imagePoint.y >= 0 && imagePoint.y < image.height) {
          if (SwingUtilities.isLeftMouseButton(e)) actionLeftClick(imagePoint)
          else if (SwingUtilities.isRightMouseButton(e)) actionRightClick(imagePoint)
        }
      }
    })
  }

  def screenToImage(screenPoint: java.awt.Point): Point[_2D] = {
    val scale = imageScale
    val (dx, dy) = imageOffset
    Point((screenPoint.x - dx)/scale, (screenPoint.y - dy)/scale)
  }

  def imageToScreen(imagePoint: Point[_2D]): java.awt.Point = {
    val scale = imageScale
    val (dx, dy) = imageOffset
    new java.awt.Point((imagePoint.x * scale).toInt + dx, (imagePoint.y * scale).toInt + dy)
  }
}

object ImagePanel {
  def apply(width: Int, height: Int, image: PixelImage[RGB]) = new ImagePanel(width, height, image)

  def apply(image: PixelImage[RGB]) = new ImagePanel(image.width, image.height, image)
}
