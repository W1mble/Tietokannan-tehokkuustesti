### Tietokannan-tehokkuustesti

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)

Simppeli ohjelma Javalla toteutettuna, jolla testataan tietokannan tehokkuutta.

**Toiminta**
- Ohjelma luo tauluun miljoona riviä, joissa nimenä on satunnainen merkkijono sekä vuotena on satunnainen kokonaisluku väliltä 1900–2000.
- Ohjelma suorittaa tuhat kertaa kyselyn, jossa haetaan elokuvien määrä vuonna x. Jokaisessa kyselyssä x valitaan satunnaisesti väliltä 1900–2000. 

**Ohjelmassa on kolme testiä**
1. Tauluun ei lisätä kyselyitä tehostavaa indeksiä.
2. Tauluun lisätään kyselyitä tehostava indeksi ennen rivien lisäämistä.
3. Tauluun lisätään kyselyitä tehostava indeksi ennen kyselyiden suoritusta. 
    
## Esivalmistelut

* *JDBC -ajuri pitää lisätä projektin luokkapolkuun. Eclipsessä se onnistuu näin:*
     - Oikeaklikkaa projektia
     - Java Built Path
     - Libraries
     - Classpath
     - Add external jar

### Vaatimukset
* **Testattu Java 15 (vanhemmatkin versiot voivat toimia)**
*  **sqlite-jdbc-3.32.3.2.jar** https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
