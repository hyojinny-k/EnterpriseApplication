import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import java.time.LocalDate;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class UBERStudent20210777 implements Serializable {

    static String getDay(String month, String date, String year) {
        String[] r = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        LocalDate d = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(date));
        int dayOfWeek = d.getDayOfWeek().getValue();
        return r[dayOfWeek - 1];
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.err.println("Usage: UBERStudent20210777 <in-file> <out-file>");
            System.exit(1);
        }

        SparkSession spark = SparkSession
                .builder()
                .appName("UBERStudent20210777")
                .getOrCreate();

        JavaRDD<String> lines = spark.read().textFile(args[0]).javaRDD();

        JavaPairRDD<String, Tuple2<Integer, Integer>> result = lines.mapToPair(
                new PairFunction<String, String, Tuple2<Integer, Integer>>() {
                    public Tuple2<String, Tuple2<Integer, Integer>> call(String s) {
                        String[] list = s.split(",");
                        String[] dayList = list[1].split("/");

                        String key = list[0] + "/" + getDay(dayList[0], dayList[1], dayList[2]);

                        return new Tuple2<>(key, new Tuple2<>(Integer.parseInt(list[2]), Integer.parseInt(list[3])));
                    }
                });

        JavaPairRDD<String, String> counts = result.reduceByKey(
                new Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>() {
                    public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> x, Tuple2<Integer, Integer> y) {
                        return new Tuple2<>(x._1() + y._1(), x._2() + y._2());
                    }
                }).mapToPair(
                new PairFunction<Tuple2<String, Tuple2<Integer, Integer>>, String, String>() {
                    public Tuple2<String, String> call(Tuple2<String, Tuple2<Integer, Integer>> pair) {
			String key = pair._1();
                        Tuple2<Integer, Integer> value = pair._2();
                        String vehicles = String.valueOf(value._1());
                        String trips = String.valueOf(value._2());
                        return new Tuple2<>(key, vehicles + "/" + trips);
                    }
                });

        counts.saveAsTextFile(args[1]);
        spark.stop();
    }
}

