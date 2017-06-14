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

import java.awt.Color
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{BorderFactory, JButton, UIManager}

/**
 * Contains components which are only useful for Landmarks clicker
 */
case class LandmarkButton(text: String) extends JButton {
  setText(text)
  setOpaque(true)

  def resetLayout() = {
    setBorder(UIManager.getBorder("Button.border"))
    setBackground(UIManager.getColor("Panel.background"))
  }

  def setActive() = {
    setBackground(Color.LIGHT_GRAY)
    setBorder(BorderFactory.createLineBorder(Color.GREEN, 2))
  }

  def setProcessed() = {
    setBackground(Color.GREEN)
    setBorder(UIManager.getBorder("Button.border"))
  }

  def onClick(clickListener: (ActionEvent) => Unit): Unit ={
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent) = clickListener(e)
    })
  }
}
