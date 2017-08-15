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

package scalismo.faces.apps

import java.io.File
import javax.swing.UIManager

import scalismo.faces.gui.controllers.LMClickerViewController

/**
 * Landmarks clicker script
 */
object LMClicker {
  def main(args: Array[String]): Unit = {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
    val workingDir = args.headOption.getOrElse(new File(".").getCanonicalPath)
    val controller = LMClickerViewController(new File(workingDir), args.drop(1).map(new File(_)))
  }
}