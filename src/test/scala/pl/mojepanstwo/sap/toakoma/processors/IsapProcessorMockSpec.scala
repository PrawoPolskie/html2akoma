package pl.mojepanstwo.sap.toakoma.processors

import pl.mojepanstwo.sap.toakoma._
import org.jsoup.Jsoup
import scala.io.Source
import java.util.Date
import java.text.SimpleDateFormat
import org.jsoup.nodes.Document
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.boot.test.context.SpringBootTest

class IsapProcessorMockSpec extends UnitSpec {

  "A IsapMockProcessor" should "parse html" in {
    val document = Jsoup.parse(Source.fromResource("isap/WDU20170000001.html").mkString)
    val isapProcessor = new IsapProcessor(new ResourceScraperService)

    val model = isapProcessor.process(document)

    model.id shouldBe "WDU20170000001"
    model.dziennik.name shouldBe "DZIENNIK_USTAW"
    model.year shouldBe "2017"
    model.dataOgloszenia.compareTo(new SimpleDateFormat("yyyy-MM-dd").parse("2017-01-02")) shouldBe 0
    val ape = model.aktyPowiazane.keysIterator
    val ap1 = ape.next
    ap1.name shouldBe "PODSTAWA_PRAWNA"
    model.aktyPowiazane.get(ap1).get(0).tytul shouldBe "Ustawa z dnia 28 kwietnia 2011 r. o systemie informacji w ochronie zdrowia"
    val ap2 = ape.next
    ap2.name shouldBe "ODESLANIA"
    model.aktyPowiazane.get(ap2).get(0).status.isap shouldBe "akt posiada tekst jednolity"
    model.aktyPowiazane.get(ap2).get(0).adres_publikacyjny shouldBe "Dz.U. 2011 nr 122 poz. 696"
    model.dataUchylenia shouldBe null
    model.dataWejsciaWZycie.compareTo(new SimpleDateFormat("yyyy-MM-dd").parse("2017-01-03")) shouldBe 0
    model.dataWydania.compareTo(new SimpleDateFormat("yyyy-MM-dd").parse("2016-12-23")) shouldBe 0
    model.dataWygasniecia shouldBe null
    model.linksPdf.get(Pdf.TEKST_OGLOSZONY) shouldBe Some(Some("/tmp/D20170001.pdf"))
    model.number shouldBe null
    model.organUprawniony should have size 0
    model.organWydajacy(0).isap shouldBe "MIN. ZDROWIA"
    model.organZobowiazany should have size 0
    model.position shouldBe "1"
    model.statusAktuPrawnego.isap shouldBe "obowiązujący"
    model.title shouldBe "Rozporządzenie Ministra Zdrowia z dnia 23 grudnia 2016 r. w sprawie minimalnej funkcjonalności Systemu Obsługi List Refundacyjnych"
    model.uwagi shouldBe null
  }

}