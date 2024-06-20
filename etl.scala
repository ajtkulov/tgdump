import org.jsoup.nodes._

object TgStringUtils {
  def norm(value: String): String = {
    value.filter(c => c != '\n' || c != '\t' || c != '\r').split(" ").filter(_.nonEmpty).mkString(" ")
  }
}

case class TGCard(channel: String, title: String, description: String, tags: Vector[String], subscribers: Int) {
  def toCsvLine = {
    Vector(channel, title, description, tags.mkString("|||"), subscribers.toString).map(TgStringUtils.norm).mkString("\t")
  }
}


object TgETL {

  def extractCardsFromFile(fileName: String, output: String, extraTags: Vector[String]) = {
    val content = scala.io.Source.fromFile(fileName, "UTF-8").getLines().mkString("\n")
    val doc: Document = org.jsoup.Jsoup.parse(content)

    val cardDivs = doc.select("div.col-12.col-sm-6.col-lg-4").toArray.map(_.asInstanceOf[Element])

    val cards = cardDivs.flatMap { item =>
      scala.util.Try {
        val link = item.select("a").toArray.map(_.asInstanceOf[Element]).map(_.attr("abs:href")).filter(_.contains("channel")).head
        val title = item.select("div.text-dark.text-truncate").toArray.map(_.asInstanceOf[Element]).head.text()
        val tag = item.select("div.font-12.text-body").toArray.map(_.asInstanceOf[Element]).head.text()
        val subscribers = item.select("div.font-12.text-truncate").toArray.map(_.asInstanceOf[Element]).head.text().filter(_.isDigit).toInt
        val description = item.select("div.line-clamp-2").toArray.map(_.asInstanceOf[Element]).head.text()
        Some(TGCard(link, title, description, extraTags :+ tag, subscribers))
      }.getOrElse(None)
    }.toVector

    FileUtils.write(output, cards.iterator.map(_.toCsvLine))
  }

  def extractCardsFromFile1(fileName: String, output: String, extraTags: Vector[String]) = {
    val content = scala.io.Source.fromFile(fileName, "UTF-8").getLines().mkString("\n")
    val doc: Document = org.jsoup.Jsoup.parse(content)

    val cardDivs = doc.select("div.col-12.col-sm-6.col-md-4").toArray.map(_.asInstanceOf[Element])

    val cards = cardDivs.flatMap { item =>
      scala.util.Try {
        val link = item.select("a").toArray.map(_.asInstanceOf[Element]).map(_.attr("abs:href")).filter(_.contains("channel")).head
        val title = item.select("div.text-dark.text-truncate").toArray.map(_.asInstanceOf[Element]).head.text()
        val tag: String = scala.util.Try {
          item.select("div.font-12.text-body").toArray.map(_.asInstanceOf[Element]).head.text()
        }.getOrElse("")
        val subscribers = item.select("div.font-12.text-truncate").toArray.map(_.asInstanceOf[Element]).head.text().filter(_.isDigit).toInt
        val description = item.select("div.line-clamp-2").toArray.map(_.asInstanceOf[Element]).head.text()
        Some(TGCard(link, title, description, extraTags :+ tag, subscribers))
      }.getOrElse(None)
    }.toVector

    FileUtils.write(output, cards.iterator.map(_.toCsvLine))
  }

  def extractGeoCardsFromFile(fileName: String, output: String) = {
    val content = scala.io.Source.fromFile(fileName, "UTF-8").getLines().mkString("\n")
    val doc: Document = org.jsoup.Jsoup.parse(content)

    val buttons = doc.select("div.col-12.col-sm-6.col-md-4").toArray.map(_.asInstanceOf[Element])

    val cards = buttons.map { item =>
      val link = item.select("a").toArray.map(_.asInstanceOf[Element]).map(_.attr("href")).head
      val title = item.select("a").toArray.map(_.asInstanceOf[Element]).map(_.text()).head
      val count = item.select("a").toArray.map(_.asInstanceOf[Element]).map(_.text()).last.filter(_.isDigit).toInt
      Vector(link, title, count.toString).map(TgStringUtils.norm).mkString("\t")
    }.toVector

    FileUtils.write(output, cards.iterator)
  }

  extractGeoCardsFromFile("geo.html", "geo.cards")

  {
    val iter = scala.io.Source.fromFile("geo.cards", "UTF-8").getLines.map { line =>
      val split = line.split("\t")
      val h = split.head.drop(1)

      s"""crawl_page("https://tgstat.ru/$h", "${h.replace('/', '_')}.html")"""
    }

    FileUtils.write("geo.geo", iter)
  }

  {
    val iter = scala.io.Source.fromFile("geo.cards", "UTF-8").getLines.map { line =>
      val split = line.split("\t")
      val h = split.head.drop(1)

      s"""extractCardsFromFile("${h.replace('/', '_')}.html", "${h.drop(4)}.tg", Vector[String]("Регион", "${split(1)}"))"""
    }

    FileUtils.write("geo.ccc", iter)
  }

  {
    extractCardsFromFile1("blogs.html", "blogs.tg", Vector[String]("Блоги"))
  }
}
