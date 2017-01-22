package ejb;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import entity.Bestellung;
import entity.Kunde;

@Stateless
public class BestellungVerwaltungEjb implements BestellungVerwaltungInt{

	@PersistenceContext
	private EntityManager em;
	
	private BigDecimal stueckPreis = new BigDecimal(0.8);

	
	/* Business - Logik */
	/*------------------*/
	
	@Override
	public BigDecimal getStueckPreis() {
		return stueckPreis;
	}

	@Override
	public void setStueckPreis(BigDecimal stueckPreis) {
		this.stueckPreis = stueckPreis;
	}	

	@Override
	public BigDecimal berechnePreisBestellung(Integer anzahl) {
		return stueckPreis.multiply(new BigDecimal(anzahl)); 
	}
	
	@Override
	// naechsten Dienstag bestimmen. Ab Samstag den uebernaechsten Dienstag bestimmen
	public Date bestimmeNaechsterLiefertermin() {
        Calendar c = Calendar.getInstance(TimeZone.getDefault(), Locale.GERMANY);

        //Timezone setzen
        c.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        
        Integer dayOfWeek = c.get(Calendar.DAY_OF_WEEK); 
        int diff = Calendar.TUESDAY - dayOfWeek;
        if (!(diff > 0)) {
            diff += 7;
        }
        if (dayOfWeek == Calendar.SUNDAY   ||
            dayOfWeek == Calendar.MONDAY) {
            diff += 7;
            }
        c.add(Calendar.DAY_OF_MONTH, diff);
        return c.getTime();
	}	
	
	/* DB - Funktionen  */
	/*------------------*/
		
	
	@Override
	public Bestellung kreiereBestellung(Integer kundeId, Integer benutzerId, Integer anzahl, Date lieferTermin, String kommentar) {

		try {
			//Kunde lesen
			Kunde kunde = (Kunde) em.find(Kunde.class, kundeId);
			Bestellung bestellung = new Bestellung();
			// Status: 1=bestellt
			bestellung.setStatus(1);
			bestellung.setKunde(kunde);
			bestellung.setFkBenutzerid(benutzerId);
			bestellung.setAnzahl(anzahl);
			bestellung.setStueckpreis(stueckPreis);
			BigDecimal preis = stueckPreis.multiply(new BigDecimal(anzahl));
			preis = preis.setScale(2, BigDecimal.ROUND_HALF_UP);
			bestellung.setPreistotal(preis);
			bestellung.setLiefertermin(lieferTermin);
			bestellung.setKommentar(kommentar);			
			// Bestelldatum speichern 				
	        bestellung.setBestelltAm(getLokaleZeit());
			em.persist(bestellung);
			Integer bid = (Integer) em.createQuery("SELECT max(b.id) from Bestellung b").getSingleResult();
			Bestellung b = em.find(Bestellung.class, bid);
			return b;
		} catch (Exception e) {			
			System.out.println("BestellungVerwaltungEjb, kreiereBestellung: Bestellung kann nicht kreiert werden." );			
			return null;
		}
	}

	@Override
	public Integer mutiereBestellung(Bestellung bestellung) {
		try {
			em.merge(bestellung);
			return 0;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb: Bestellung kann nicht gemerged werden. Id = " + bestellung.getId());		
			return -1;
		}
	}

	@Override
	public Integer storniereBestellung(Bestellung bestellung) {
		try {
			//Status: 4 = storniert
			bestellung.setStatus(4);
			// Bestelldatum speichern 				
			bestellung.setStorniertAm(getLokaleZeit());
			em.merge(bestellung);
			return 0;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb: Stornodatum kann nicht gemerged werden. Id = " + bestellung.getId());		
			return -1;
		}
	}

	@Override
	public List<Bestellung>	leseBestellungenZumBestaetigen(Boolean inklBestaetigte) {
		try {
			
			//Bestellung.status: 1 = bestellt, 2=bestaetigt, 3=geliefert, 4=abgeschlossen, 5=storniert
			Integer status;
			if (inklBestaetigte)
				status = 2;
			else
				status = 1;
			@SuppressWarnings("unchecked")
			List<Bestellung> b = em.createQuery("SELECT b from Bestellung b "
					                           + "where status <= ?1 "
					                           + "order by liefertermin")
					                           .setParameter(1, status)
					                           .getResultList();		
			return b;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb.leseBestellungenZumBestaetigen: "
					+ "Bestellungen zum Bestaetigen konnten nicht gelesen werden.");
			System.out.println(e.getMessage());
			return null;
		}
		
	}
	
	
	@Override
	public List<Bestellung> leseBestellungenOffenByKundeId(Integer kundeId) {
		try {
			
			//Bestellung.status: 1 = bestellt, 2=bestaetigt, 3=geliefert, 4=abgeschlossen, 5=storniert
			@SuppressWarnings("unchecked")
			List<Bestellung> b = em.createQuery("SELECT b from Bestellung b "
					                           + "where fk_kundeid =?1 "
					                           + "and status < 4  "
					                           + "order by id")
					                           .setParameter(1, kundeId)
					                           .getResultList();		
			return b;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb.leseBestellungenOffenByKundeId: "
					+ "Bestellungen konnten nicht gelesen werden. KundeId = " + kundeId);
			return null;
		}
	}

	@Override
	public List<Bestellung> leseBestellungenAlleByKundeId(Integer kundeId, Integer limit, Integer skip) {
		try {			
			//Bestellung.status: 1 = bestellt, 2=bestaetigt, 3=geliefert, 4=storniert
			@SuppressWarnings("unchecked")
			List<Bestellung> b = em.createQuery("SELECT b from Bestellung b "
					                           + "where fk_kundeid =?1 "
					                           + "order by bestellt_am desc limit ?2 offset ?3")
					                           .setParameter(1, kundeId)
					                           .setParameter(2, limit)
					                           .setParameter(3, skip)
					                           .getResultList();		
			return b;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb.leseBestellungenAlleByKundeId: "
					+ "Bestellungen konnten nicht gelesen werden. KundeId = " + kundeId);
			return null;
		}
	}

	@Override
	public List<Bestellung> leseLieferungOffen() {
		try {			
			//Bestellung.status: 2=bestaetigt
			@SuppressWarnings("unchecked")
			List<Bestellung> b = em.createQuery("SELECT b from Bestellung b "
					                           + "where status = 2 "
					                           + "order by liefertermin desc")
					                           .getResultList();
			return b;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb.leseLieferungOffen: "
					+ "Bestellungen konnten nicht gelesen werden." );
			return null;
		}
	}

	@Override
	public List<Bestellung> leseBezahlungOffen() {
		try {			
			//Bestellung.status: 3=geliefert
			@SuppressWarnings("unchecked")
			List<Bestellung> b = em.createQuery("SELECT b from Bestellung b "
                    							+ "where   status = 3 "
                    							+ "order by geliefert_am desc")
                    							.getResultList();		
			return b;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb.leseBezahlungOffen: "
					+ "Bestellungen konnten nicht gelesen werden." );
			return null;
		}
	}

	@Override
	public Kunde leseKundeZuBestellung(Integer bestellungId) {
		try {			
			Bestellung b = em.find(Bestellung.class, bestellungId);			
			Kunde k = b.getKunde();	
			return k;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb.leseKundeZuBestellung: "
					+ "Bestellungen konnten nicht gelesen werden. BestellungId = " + bestellungId);
			return null;
		}
	}

	public Integer lieferterminBestaetigen(Integer bestellungId, Date lieferTerminBestaetigt) {
		
		try {
			Bestellung b = em.find(Bestellung.class, bestellungId);
			//status: 2=bestaetigt
			b.setStatus(2);
			b.setBestaetigtAm(getLokaleZeit());
			b.setLieferterminBestaetigt(lieferTerminBestaetigt);
			return 0;
		} catch (Exception e) {
			System.out
					.println("BestellungVerwaltungEjb.lieferterminBestaetigen(): "
							+ "Bestaetigter Liefertermin kann nicht gespeichert werden. BestellungId = "
							+ bestellungId);
			return -1;
		}
	}
	
	@Override
	public Integer lieferungAbschliessen(Integer bestellungId, Boolean bezahlt) {
		try {			
			Bestellung b = em.find(Bestellung.class, bestellungId);	
			//status: 3=geliefert
			b.setStatus(3);	
	        b.setGeliefertAm(getLokaleZeit());
			if (bezahlt) {
		        b.setBezahltAm(getLokaleZeit());
		        //BelastungGuthaben: 1=nein, bar bezahlt
		        b.setBelastungGuthaben(1);
		        //status: 4=abgeschlossen
				b.setStatus(4);	
			}
			em.merge(b);
			return 0;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb.lieferungAbschliessen: "
					+ "Lieferung kann nicht abgeschlossen werden. Id= " + bestellungId);
			return -1;
		}
	}
	
	@Override
	public Integer lieferungAbschliessenBelastungGuthaben(Integer bestellungId) {
		try {			
			Bestellung b = em.find(Bestellung.class, bestellungId);	
			//status: 3=geliefert
			b.setStatus(3);	
	        b.setGeliefertAm(getLokaleZeit());
		    b.setBezahltAm(getLokaleZeit());
		    //BelastungGuthaben: 2 = ja, Guthaben belastet
		    b.setBelastungGuthaben(2);
		    //status: 4=abgeschlossen
			b.setStatus(4);	
			em.merge(b);
			
			//Kundenguthaben belasten
			Kunde k = b.getKunde();
			BigDecimal preisLieferung = b.getPreistotal();
			BigDecimal guthaben = k.getGuthaben();
			k.setGuthaben(guthaben.subtract(preisLieferung));			
			em.merge(k);
			return 0;
		} catch (Exception e) {
			System.out.println("BestellungVerwaltungEjb.lieferungAbschliessenBelastungGuthaben: "
					+ "Lieferbestaetigung kann nicht gespeichert werden. Id= " + bestellungId);
			return -1;
		}
	}


	@Override
	public Integer bestellungBezahltSetzen(Integer bestellungId) {
		
		try {
			Bestellung b = em.find(Bestellung.class, bestellungId);
			//status: 4=abgeschlossen
			b.setStatus(4);
			//BelastungGuthaben: 1 = bar bezahlt
			b.setBelastungGuthaben(1);
			b.setBezahltAm(getLokaleZeit());
			em.merge(b);
			return 0;
		} catch (Exception e) {
			System.out
					.println("BestellungVerwaltungEjb.bestellungBezahltSetzen(): "
							+ "Bestellung konnte nicht abgeschlossen werden. BestellungId = "
							+ bestellungId);
			return -1;
		}
	}
	
	public Integer bestellungStorniertSetzen(Integer bestellungId) {
		
		try {
			Bestellung b = em.find(Bestellung.class, bestellungId);
			//status: 5=storniert
			b.setStatus(5);
			b.setStorniertAm(getLokaleZeit());
			em.merge(b);
			return 0;
		} catch (Exception e) {
			System.out
					.println("BestellungVerwaltungEjb.bestellungStorniertSetzen(): "
							+ "Bestellung konnte nicht auf storniert gesetzt werden. BestellungId = " + bestellungId);
			return -1;
		}
		
	}
	
	
	
	/* Helper-Methoden */
	/*-----------------*/

	// aktuelle lokale Zeit in UTC liefern
	@SuppressWarnings("unused")
	private Date datumZeitUTC() {		
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getDefault());
		Date localDate = new Date();				
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));	
		String dateUTCStr = sdf.format(localDate);
		sdf.setTimeZone(TimeZone.getDefault());				
		Date dateUTC;
		try {
			dateUTC = sdf.parse(dateUTCStr);
			return dateUTC;
		} catch (ParseException e) {
			System.out.println("BestellungVerwaltungEjb.datumZeitUTC(): Datum kann nicht konvertiert werden.");
			return null;
		}				
		
	}
	
	// Host-Time in lokale Zeit transformieren (Berlin)
	private Date getLokaleZeit() {
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getDefault());
		Date hostDate = new Date();				
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));	
		String dateBerlinStr = sdf.format(hostDate);
		sdf.setTimeZone(TimeZone.getDefault());				
		Date dateBerlin;
		try {
			dateBerlin = sdf.parse(dateBerlinStr);
			return dateBerlin;
		} catch (ParseException e) {
			System.out.println("BestellungVerwaltungEjb.getBerlinTime(): Datum kann nicht konvertiert werden.");
			return null;
		}				
		
	}
	
}
