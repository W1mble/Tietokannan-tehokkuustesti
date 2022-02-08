import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

/**
 * @author Ville Selkämaa
 * @version 08.08.2022
 *
 */
public class SQL {

	private static final String KIRJAIMET = "abcdefghijklmnopqrstuvwxyzåäö";

	/**
	 * Pääohjelma, jossa kutsutaan 3 eri testitapausta
	 */
	public static void main(String[] args) {
		System.out.printf("Aloitetaan. Tässä saattaa mennä yli minuutti.%n");
		testi1();
		testi2();
		testi3();
		deletefiles();
	}

	/**
     * Alihojelma, jolla poistetaan syntyneet tietokannat
     */
	private static void deletefiles() {
		Path path = FileSystems.getDefault().getPath("./testi.db");
		Path path2 = FileSystems.getDefault().getPath("./testi2.db");
		Path path3 = FileSystems.getDefault().getPath("./testi3.db");
		try {
			Files.delete(path);
			Files.delete(path2);
			Files.delete(path3);
			System.out.printf("%nTietokannat poistettu");
		} catch (NoSuchFileException x) {
			System.err.format("%s: no such" + " file or directory%n", path);
		} catch (IOException x) {
			System.err.println(x);
		}
	}

	/**
	 * Testi 1, jossa ei käytetä indeksiä
	 */
	private static void testi1() {
		try (Connection db = DriverManager.getConnection("jdbc:sqlite:testi.db"); Statement s = db.createStatement();) {
			s.execute("CREATE TABLE Elokuvat (id INTEGER PRIMARY KEY, nimi TEXTT, vuosi INTEGER)");
			s.execute("BEGIN");

			long insertti = System.nanoTime();

			for (int i = 0; i < 1000000; i++) {
				s.execute("INSERT INTO Elokuvat (nimi,vuosi) VALUES ('" + randomNimi() + "'," + randomLuku() + ")");
			}
			s.execute("COMMIT");

			long kesti = System.nanoTime() - insertti;

			long hakuAlku = System.nanoTime();
			for (int i = 0; i < 1000; i++) {
				s.executeQuery("SELECT COUNT(nimi) FROM Elokuvat WHERE vuosi = " + randomLuku() + "");
			}

			long hakuLoppu = System.nanoTime() - hakuAlku;

			double hakuSekunneissa = (double) hakuLoppu / 1000000000;
			double inserttiSekunneissa = (double) kesti / 1000000000;
			System.out.printf("%nAikaa inserttiin meni ilman indeksiä: %s sekuntia%n", inserttiSekunneissa);
			System.out.printf("Aikaa hakuun meni ilman indeksiä: %s sekuntia%n", hakuSekunneissa);

			String tiedostonNimi = "testi.db"; // Tähän suhteellinen sijainti jos buildataan exeksi
			Path tiedostoSijainti = Paths.get(tiedostonNimi);
			long tiedostoKoko = Files.size(tiedostoSijainti);
			System.out.format("Tiedoston koko on: %d tavua%n%n", tiedostoKoko);
		} catch (SQLException e1) {
			System.out.println("Jokin meni vikaan SQL:n kanssa, Palauta virheilmoitus osoitteeseen: xxxxx");
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("Jokin meni vikaan db-tiedoston kanssa. Palauta virheilmoitus osoitteeseen: xxxxx");
			e.printStackTrace();
		}
	}

	/**
	 * Testi 2, jossa indeksi lisätään ennen rivien lisäystä
	 */
	private static void testi2() {
		try (Connection db2 = DriverManager.getConnection("jdbc:sqlite:testi2.db");
				Statement s2 = db2.createStatement();) {
			s2.execute("CREATE TABLE Elokuvat (id INTEGER PRIMARY KEY, nimi TEXT, vuosi INTEGER)");

			s2.execute("CREATE INDEX idx_elokuvat ON Elokuvat (vuosi)");
			long insertti2 = System.nanoTime();
			s2.execute("BEGIN");

			for (int i = 0; i < 1000000; i++) {
				s2.execute("INSERT INTO Elokuvat (nimi,vuosi) VALUES ('" + randomNimi() + "'," + randomLuku() + ")");
			}

			s2.execute("COMMIT");
			long kesti2 = System.nanoTime() - insertti2;

			long hakuAlku2 = System.nanoTime();
			for (int i = 0; i < 1000; i++) {
				s2.executeQuery("SELECT COUNT(nimi) FROM Elokuvat WHERE vuosi = " + randomLuku() + "");
			}

			long hakuLoppu2 = System.nanoTime() - hakuAlku2;

			double hakuSekunneissa2 = (double) hakuLoppu2 / 1000000000;
			double inserttiSekunneissa2 = (double) kesti2 / 1000000000;

			String tiedostonNimi2 = "testi2.db"; // Tähän suhteellinen sijainti jos buildataan exeksi
			Path tiedostoSijainti2 = Paths.get(tiedostonNimi2);
			long tiedostoKoko2 = Files.size(tiedostoSijainti2);

			System.out.printf("Aikaa inserttiin meni %s sekuntia kun indeksi lisättiin ennen rivien lisäämistä%n",
					inserttiSekunneissa2);
			System.out.printf("Aikaa hakuun meni %s sekuntia kun indeksi lisättiin ennen rivien lisäämistä%n",
					hakuSekunneissa2);
			System.out.format("Tiedoston 2 koko on: %d tavua%n%n", tiedostoKoko2);
		} catch (SQLException e) {
			System.out.println("Jokin meni vikaan SQL:n kanssa, Palauta virheilmoitus osoitteeseen: xxxxx");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Jokin meni vikaan db-tiedoston kanssa. Palauta virheilmoitus osoitteeseen: xxxxx");
			e.printStackTrace();
		}
	}

	/**
	 * Testi 3, jossa indeksi lisätään ennen kyselyitä
	 */
	private static void testi3() {
		try (Connection db3 = DriverManager.getConnection("jdbc:sqlite:testi3.db");
				Statement s3 = db3.createStatement();) {

			s3.execute("CREATE TABLE Elokuvat (id INTEGER PRIMARY KEY, nimi TEXT, vuosi INTEGER)");

			long insertti3 = System.nanoTime();
			s3.execute("BEGIN");
			for (int i = 0; i < 1000000; i++) {
				s3.execute("INSERT INTO Elokuvat (nimi,vuosi) VALUES ('" + randomNimi() + "'," + randomLuku() + ")");
			}
			s3.execute("COMMIT");
			long kesti3 = System.nanoTime() - insertti3;

			s3.execute("CREATE INDEX idx_elokuvat ON Elokuvat (vuosi)");

			long hakuAlku3 = System.nanoTime();
			for (int i = 0; i < 1000; i++) {
				s3.executeQuery("SELECT COUNT(nimi) FROM Elokuvat WHERE vuosi = " + randomLuku() + "");
			}
			long hakuLoppu3 = System.nanoTime() - hakuAlku3;

			double hakuSekunneissa3 = (double) hakuLoppu3 / 1000000000;
			double inserttiSekunneissa3 = (double) kesti3 / 1000000000;

			String tiedostonNimi3 = "testi2.db"; // Tähän suhteellinen sijainti jos buildataan exeksi
			Path tiedostoSijainti3 = Paths.get(tiedostonNimi3);
			long tiedostoKoko3 = Files.size(tiedostoSijainti3);

			System.out.printf("Aikaa inserttiin meni %s sekuntia kun indeksi lisättiin rivien lisäämisen jälkeen%n",
					inserttiSekunneissa3);
			System.out.printf("Aikaa hakuun meni %s sekuntia kun indeksi lisättiin rivien lisäämisen jälkeen%n",
					hakuSekunneissa3);
			System.out.format("Tiedoston 3 koko on: %d tavua%n", tiedostoKoko3);
		} catch (SQLException e) {
			System.out.println("Jokin meni vikaan SQL:n kanssa, Palauta virheilmoitus osoitteeseen: xxxxx");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Jokin meni vikaan db-tiedoston kanssa. Palauta virheilmoitus osoitteeseen: xxxxx");
			e.printStackTrace();
		}
	}

	/**
	 * @return joku luku väliltä 1900-2000
	 */
	public static int randomLuku() {
		Random rand = new Random();
		return rand.nextInt(100) + 1900;
	}

	/**
	 * @return satunnainen generoitu 6-kirjaiminen nimi
	 */
	public static String randomNimi() {
		Random rand = new Random();
		StringBuilder nimi = new StringBuilder(6);
		for (int i = 0; i < 5; i++) {
			nimi.append(KIRJAIMET.charAt(rand.nextInt(KIRJAIMET.length())));
		}
		return nimi.toString();
	}
}
