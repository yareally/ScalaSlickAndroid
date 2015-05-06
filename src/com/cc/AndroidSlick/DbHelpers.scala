package com.cc.AndroidSlick

import java.sql.{Driver, DriverManager}
import android.util.Log
import slick.profile.SqlProfile.ColumnOption.NotNull
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.slick.driver.SQLiteDriver.api._

/**
 * Methods for testing Slick
 *
 * The Slick calls to methods not implemented when trying
 * to create tables programmatically instead of through queries
 * as done in this example.
 *
 * Not creating tables through queries will work, but
 * you'll end up with tables missing indices and foreign keys.
 *
 * I haven't done any serious testing yet to test other features though.
 *
 * @author Wes Lanning
 * @version 2015-05-02
 */
object DbHelpers {
  // using https://www.sqlite.org/android/doc/trunk/www/index.wiki
  // instead of the built in SQLite so it's more up to date and less bugs
  // remove this line if not using those libraries
  System.loadLibrary("sqliteX")

  case class Animal(id: Int, speciesId: Int, name: String)

  // Yes, I know species incorrectly spelled.
  // I have better things to do than to bikeshed what to do when
  // singular and plural are the same word for tiny example project
  case class Specie(id: Int, name: String)

  private var db: Database = null

  private lazy val animals = TableQuery[Animals]
  private lazy val species = TableQuery[Species]

  def init(dbLocation: String, dbName: String, driver: String = "org.sqldroid.SQLDroidDriver") {
    DriverManager.registerDriver(Class.forName(driver, true, getClass.getClassLoader).newInstance.asInstanceOf[Driver])
    Log.d(DbHelpers.getClass.getName, s"Path to db: jdbc:sqlite:$dbLocation/$dbName")

    org.sqldroid.Log.LEVEL = android.util.Log.VERBOSE
    db = Database.forURL(s"jdbc:sqlite:$dbLocation/$dbName", driver, keepAliveConnection = true)

    val result: DBIO[Option[Int]] = sql"""SELECT COUNT(name) FROM sqlite_master WHERE type='table'""".as[Int].headOption
    val dbCreated = Await.result(db.run(result), 5.seconds).exists((x: Int) ⇒ x > 0)

    if (!dbCreated) {
      Log.d(DbHelpers.getClass.getName, s"db $dbName has not been created. Creating now....")

       val tblSpecies = sqlu"""
       CREATE TABLE species (
       _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
       name TEXT NOT NULL)"""


       val tblAnimals = sqlu"""
        CREATE TABLE animals (
        _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        species_id INTEGER NOT NULL,
        name TEXT NOT NULL,
        CONSTRAINT fkey0 FOREIGN KEY (species_id) REFERENCES species (_id) ON DELETE CASCADE ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED)"""

      val animalsIdx = sqlu"""CREATE UNIQUE INDEX idx_animals_name ON animals (name ASC)"""
      val speciesIdx =sqlu"""CREATE UNIQUE INDEX idx_species_name ON species (name ASC)"""

      val setup = DBIO.seq(
        species ++= Seq(
          Specie(1, "Bird"),
          Specie(2, "Mammal"),
          Specie(3, "Fish"),
          Specie(4, "Reptile")
        ),

        animals ++= Seq(
          Animal(1, 1, "Blue Jay"),
          Animal(2, 2, "Polar Bear"),
          Animal(3, 3, "Whale Shark"),
          Animal(4, 4, "King Cobra")
        )
      )
      val tblFuture = for {
        _ ← db.run(tblSpecies)
        _ ← db.run(tblAnimals)
        _ ← db.run(animalsIdx)
        _ ← db.run(speciesIdx)
        _ ← db.run(setup)
        r ← db.run( sqlu"""DELETE FROM species WHERE _id = 4""")
      } yield r

      tblFuture.onFailure { case s ⇒
        Log.wtf(DbHelpers.getClass.getName, s"db tables not created. Reason: $s")
      }

      tblFuture.onSuccess { case s ⇒
        Log.d(DbHelpers.getClass.getName, s"tables created!")
      }
    }
  }

  class Animals(tag: Tag) extends Table[Animal](tag, "animals") {
    def id = column[Int]("_id", O.PrimaryKey, O.AutoInc)
    def speciesId = column[Int]("species_id", NotNull)
    def name = column[String]("name", NotNull)
    def nameIndex = index("idx_animals_name", name, unique = true)
    def fkSpecies = foreignKey("fk_animals_species", speciesId, species)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def * = (id, speciesId, name) <>(Animal.tupled, Animal.unapply)
  }

  class Species(tag: Tag) extends Table[Specie](tag, "species") {
    def id = column[Int]("_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", NotNull)
    def * = (id, name) <>(Specie.tupled, Specie.unapply)
    def nameIndex = index("idx_species_name", name, unique = true)
  }
}
