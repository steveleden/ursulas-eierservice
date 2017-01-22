package ejb;

//import javax.ejb.Schedule;
import javax.ejb.Singleton;

@Singleton
public class ScheduledTasks {
	
	//Damit Schedule funktioniert, muss der Server mit der Configuration standalone-preview.xml gestartet werden.
	//Auf Server doppelklicken, 'Runtime environment' anklicken, Configuration file auswaehlen.
	//Falls beim Starten des Servers wegen des Timers ein Error erzeugt wird,
	//Files in JBOSS_HOME/standalone/data/time-service-data loeschen
//	@Schedule(second = "*/10", minute = "*", hour = "*", persistent=false)
//	public void someTask() {
//		//System.out.println("EggStore: Test scheduler");
//	}
}
