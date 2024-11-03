import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EndToEndIntegrationSpec extends AnyFlatSpec with Matchers {
  "End-to-end text generation" should "work with trained model" in {
    val sc = new SparkContext(new SparkConf().setMaster("local[2]").setAppName("test"))
    try {
      // Training phase
      val textRDD = sc.parallelize(Seq(
        "the quick brown fox jumps over the lazy dog",
        "a quick brown dog jumps over the fox",
        "the lazy fox sleeps while the dog jumps"
      ))

      val train = new Train()
      val metricsWriter = new java.io.BufferedWriter(
        new java.io.FileWriter("src/test/resources/output/e2e-metrics.csv")
      )

      val model = train.train(sc, textRDD, metricsWriter, 1)

      // Text generation phase
      val tokenizer = new Tokenizer()
      tokenizer.fit(textRDD.collect())

      val textOutput = new TextOutput()
      val generatedText = textOutput.generateText(model, tokenizer, "the quick", 500)

      generatedText should not be empty
    } finally {
      sc.stop()
    }
  }
}
