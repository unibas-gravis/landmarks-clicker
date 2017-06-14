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

import java.awt.{BorderLayout, Component, Dimension}
import java.io.File
import javax.swing.event.{TreeSelectionEvent, TreeSelectionListener}
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.{JPanel, JScrollPane, JTree}

class FileTree(dir: File) extends JPanel{
  /** Construct a FileTree */

  var tree: JTree = new JTree(addNodes(null, dir))
  var onFileClickedAction:(String => Unit) = s => ()

  setLayout(new BorderLayout)
  val scrollpane: JScrollPane = new JScrollPane
  refreshTree

  private def refreshTree: Component = {
    tree = new JTree(addNodes(null, dir))
    scrollpane.getViewport.add(tree)
  }

  add(BorderLayout.CENTER, scrollpane)

  def onFileClicked(action: String => Unit) = {
    onFileClickedAction = action
    updateListener()
  }

  def update() = {
    refreshTree
    updateListener()
    tree.updateUI()
  }

  def updateListener(): Unit = {
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      def valueChanged(e: TreeSelectionEvent) {
        val node = e.getPath.getPath.foldLeft("")((last, cur) => if (last != "") last + File.separator + cur else cur.toString)
        onFileClickedAction(node)
      }
    })
  }

  /** Add nodes from under "dir" into curTop. Highly recursive. */
  def addNodes(curTop: DefaultMutableTreeNode, dir: File): DefaultMutableTreeNode = {
    val curPath: String = dir.getPath
    val curDir: DefaultMutableTreeNode = new DefaultMutableTreeNode(curPath)

    def recursiveListFiles(currentNode: DefaultMutableTreeNode, path: String):DefaultMutableTreeNode = {
      val these = new File(path).listFiles()
      these.foreach(file => {
        if(file.isDirectory){
          val subDir = new DefaultMutableTreeNode(file.getName)
          recursiveListFiles(subDir, file.getAbsolutePath)
          currentNode.add(subDir)
        } else
          currentNode.add(new DefaultMutableTreeNode(file.getName))
      })
      currentNode
    }
    recursiveListFiles(curDir, curPath)
  }

  override def getMinimumSize: Dimension = {
    new Dimension(200, 400)
  }

  override def getPreferredSize: Dimension = {
    new Dimension(200, 400)
  }
}
