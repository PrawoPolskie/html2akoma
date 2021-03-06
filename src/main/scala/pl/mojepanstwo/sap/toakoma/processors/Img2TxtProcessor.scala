package pl.mojepanstwo.sap.toakoma.processors

import java.io.{File, FileInputStream, InputStreamReader}

import pl.mojepanstwo.sap.toakoma._

import scala.xml.{Elem, Node, XML}

import scala.xml.transform.RewriteRule
import sys.process._


class Img2TxtProcessor extends Model2ModelProcessor {

  override def process(item:Model): Model = {

    try {
      val xml = XML.load(new InputStreamReader(new FileInputStream(item.xmlPath), "UTF-8"))

      val changed = new RewriteRule {
        override def transform(n: Node): Seq[Node] = n match {
          case elem @ Elem(_, "img", _, _, child @ _*) => {
            val src = elem.attribute("src").get
            val imageFile = new File(item.linkHtml + "/" + src)

            val cmd = "tesseract " +
              imageFile.getAbsolutePath + " " +
              "stdout " +
              "-l pol"
            var result = cmd !!

            if(Option(result).exists(_.trim.isEmpty)) return n

            result = result.replaceAll("-\n", "")
            result = result.replaceAll("—\n", "")

            return(<textFromImg>{result}</textFromImg>)
          }
          case elem: Elem => elem copy (child = elem.child flatMap (this transform))
          case other => other
        }
      } transform xml

        XML.save(item.linkHtml + "/text_from_image.xml", changed(0), "UTF-8", true)
        item.xmlPath = item.linkHtml + "/text_from_image.xml"
      } catch {
        case e: Throwable => println(e.printStackTrace())
      }

    item
  }
}
