package web;

import java.io.IOException;
import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import web.BenutzerBean.BenutzerRolle;
import ejb.AuthorisierungInt;
import ejb.BenutzerVerwaltungInt;
import ejb.KundenVerwaltungInt;
import entity.Kunde;

@ManagedBean
@SessionScoped
public class AuthController implements Serializable {

	private static final long serialVersionUID = 8800130583675966556L;

	/* CDI - Dependency */
	@ManagedProperty(value="#{benutzerBean}")
	BenutzerBean benutzer;
	
	@EJB
	AuthorisierungInt authBean;
	
	@EJB
	BenutzerVerwaltungInt benutzerVerwaltung;
	
	@EJB
	KundenVerwaltungInt kundenVerwaltung;
	
	public void setBenutzer(BenutzerBean benutzer) {
		this.benutzer = benutzer;
	}

	private String password = null;
	//private String _pw = "testpwd";
	
	
	public AuthController () {
		this.setPassword("testpwd");
	}
	
	
	/* Setters & Getters */
	
	public String getPassword() {
		return this.password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	/* Controller */
	
	/* Check password method */
	public String login() {

		Integer rolle = authBean.authorisiert(benutzer.getName(), this.password);
		
		if (rolle > 0) {
			benutzer.setAuthorisiert(true);
			if (rolle == 2) {
				benutzer.setRolle(BenutzerRolle.ADMIN);
				benutzer.setIstAdministrator(true);
			} else {
				benutzer.setRolle(BenutzerRolle.KUNDE);	
			}
			benutzerVerwaltung.updateLoginInfo(benutzer.getName());
			if (rolle == 1) {
				Kunde kunde = kundenVerwaltung.leseKundeByBenutzerName(benutzer.getName());
				if (kunde == null) {
					return "loginFailure";
				}
				benutzer.setKundenId(kunde.getId());
				benutzer.setGuthaben(kunde.getGuthaben());
			}
			return "loggedIn";
		}
		System.out.println("auth fehler");
		benutzer.setAuthorisiert(false);
		FacesContext.getCurrentInstance().addMessage("frm1:the_password", new FacesMessage("Passwort falsch"));
		FacesContext.getCurrentInstance().addMessage("frm2:the_password", new FacesMessage("Passwort falsch"));
		return "loginFailure";

	}
	
	/* Benutzer Logout */
	public String logout() throws IOException {
		benutzer.setAuthorisiert(false);
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();		
		return "loggedOut";
	}
	
}
