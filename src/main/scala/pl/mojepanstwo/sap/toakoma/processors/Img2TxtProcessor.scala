package pl.mojepanstwo.sap.toakoma.processors

import java.io.{File, FileInputStream, InputStreamReader}

import org.springframework.batch.item.ItemProcessor
import pl.mojepanstwo.sap.toakoma.IsapModel

import scala.xml.{Elem, Node, XML}

import scala.xml.transform.RewriteRule
import sys.process._


class Img2TxtProcessor extends ItemProcessor[IsapModel, IsapModel] {

  override def process(item:IsapModel): IsapModel = {

    item.linksHtml.foreach { case (key, dirPath) =>
      try {
        val xml = XML.load(new InputStreamReader(new FileInputStream(dirPath + "/1.xml"), "UTF-8"))

        new RewriteRule {
          override def transform(n: Node): Seq[Node] = n match {
            case elem @ Elem(_, "img", _, _, child @ _*) => {
              val src = elem.attribute("src").get
              val imageFile = new File(dirPath + "/" + src)

              val cmd = "tesseract " +
                imageFile.getAbsolutePath + " " +
                "stdout " +
                "-l pol"
              val result = cmd !!

              if(Option(result).exists(_.trim.isEmpty)) return(n)


              n
            }
//            case <version>{v}</version> if v.text contains "SNAPSHOT" => <version>{v.text.split("-")(0)}</version>
            case elem: Elem => elem copy (child = elem.child flatMap (this transform))
            case other => other
          }
        } transform xml

        XML.save(dirPath + "/2.xml", xml, "UTF-8", true)
      } catch {
        case e: Throwable => println(e.printStackTrace())
      }
    }

    item
  }
}
