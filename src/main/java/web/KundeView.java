package web;

import java.util.ArrayList;
import java.util.List;

import entity.Kunde;

public class KundeView {
	
	protected Boolean		     checked = false;
	protected Kunde			     kunde = new Kunde();
	protected List<BenutzerView> benutzerListe = new ArrayList<BenutzerView>();

	
	/* Setter & Getter */
	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	public Kunde getKunde() {
		
		return kunde;
	}

	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}

	
	public List<BenutzerView> getBenutzerListe() {	
		return benutzerListe;
	}

	public void setBenutzerListe(List<BenutzerView> benutzerListe) {
		this.benutzerListe = benutzerListe;
	}
}
