package de.bewild.touchpic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;




import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
//import android.widget.RelativeLayout;
import android.widget.TextView;

public class TouchPicActivity extends Activity implements OnClickListener {

	/*TouchPic
	 * V1.0 02.01.2014
	 * V1.1 04.01.2014 permission requirement false
	 * V1.2 04.01.2014 camera permission geändert
	 * 
	 *Lustiges Reaktionsspiel auch für Kleinkinder geeignet. Die Kinder müssen auf den Bildschirm tippen sobald ihr eigenes Bild erscheint. Ein Spiel für die ganze Familie.
Nach dem einblenden des eigenen Bildes, muss schnell der Bildschirm angetippt werden. Die Reaktionszeit wird sofort danach angezeigt und kann in einem HighScore verglichen werden. 
	 */
//private static final String BitmapDrawable = null;
	/*	private File filepfad= new File(Environment.getExternalStorageDirectory()
            + "/Android/data/"
            + getApplicationContext().getPackageName()
            + "/Files"); 
	*/
	private File filepfad=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
	private String pfad = filepfad.getAbsolutePath();
	//private boolean timer=false;
	//private boolean pic=false;
	
	private ImageView bild,pokal, platz1, platz2, platz3, platz4, platz5, platz6, platz7, platz8, platz9, platz10, siegerbild;
	
	private BitmapFactory.Options options;
	
	private String myJpgPath ;
	private ListenEintrag[] platz;
	private ListenEintrag aktuelle;

	
	private Bitmap bm, thumbnail;
	private long startTime;
	private TextView p1, p2, p3, p4, p5, p6, p7, p8, p9, p10 ;
	private TextView platz1_text, platz2_text,platz3_text,platz4_text,platz5_text,platz6_text,platz7_text,platz8_text,platz9_text, platz10_text; 
	
	private TextView anzahlspielertext, anzahlspieler, pokal_text;
	
	int CAMERA_PIC_REQUEST = 1337, spieleranzahl=0, rundenzaehler=0, maxrunde=2; 
	
	Intent cameraIntent;
	
	private volatile boolean keepRunning=false;
    	
	private Button fotobutton, spielbutton;
	
	Bitmap bmsmall;
	Thread bildanzeigen;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_touchpic);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //auf Hochformat festlegen
		  
		//Spieleranzahl wiederherstellen wenn die App in den Vordergrund kommt
		  if (savedInstanceState != null){
			    spieleranzahl = savedInstanceState.getInt("anzahlspieler");
			  //SpielButton einblenden wenn mehr als 2 Fotos aufgenommen
		    	if(spieleranzahl>=2){
					spielbutton.setVisibility(View.VISIBLE);
				}
			  }
		
		
		//Kamera Activity Aufruf vorbereiten
		cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		
		fotobutton = (Button)findViewById(R.id.button1);
		spielbutton = (Button)findViewById(R.id.button2);
		spielbutton.setVisibility(View.INVISIBLE);
		
		initSiegerehrung();
		hideSiegerehrung();
		
		
		
		anzahlspielertext = (TextView)findViewById(R.id.anzahlspielertext);
  		anzahlspieler = (TextView)findViewById(R.id.anzahlspieler);
  		
        anzahlspieler.setText(Integer.toString(spieleranzahl));
		
	/*	
	    // Create the storage directory if it does not exist
	    if (! filepfad.exists()){
	        if (! filepfad.mkdirs()){
	           // return null;
	        }
	    } 
		*/
		bild = (ImageView)findViewById(R.id.bild);
		InitHighScore();
		HideHighScore();
//		reaktionszeit = (TextView) findViewById(R.id.reaktionszeit);
//		reaktionszeit.setVisibility(View.INVISIBLE);			 		   	        
		
		
		options = new BitmapFactory.Options();
        options.inSampleSize = 1; //auflösung ändern 1=original
	
      /*  
        java.util.Random random = new java.util.Random();
		int bildnummer =1+ random.nextInt(bereich); 
	
		//myJpgPath = pfad + "/camera/"+bildnummer+".jpg";
		//myJpgPath = filepfad.getAbsolutePath()+"/"+bildnummer+".jpg";
		myJpgPath = pfad + "/"+bildnummer+".jpg";
					
			
		System.out.println("Pfad: "+ myJpgPath); 
				
    		   
        bm = BitmapFactory.decodeFile(myJpgPath, options);
		
	    
	    bild.setImageBitmap(bm);
	   bild.setVisibility(View.VISIBLE);

	    */
	    		
				

		
	}
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		//wird aufgerufen wenn die App in den Hintergrund geht
	   super.onSaveInstanceState(outState);
	   outState.putInt("anzahlspieler", spieleranzahl); //Anzahl Spieler ablegen
	}
	
	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
		
		//wird aufgerufen wenn die App aus dem Hintergrund kommt
		super.onRestoreInstanceState(savedInstanceState);

        spieleranzahl=savedInstanceState.getInt("anzahlspieler");
    }
	
	
	private void initSiegerehrung() {
		// Initialisierung der Items für die Siegerehrung
		
		pokal = (ImageView)findViewById(R.id.pokal);
		pokal.setVisibility(View.INVISIBLE);
		
		pokal_text = (TextView)findViewById(R.id.pokal_text);
		pokal_text.setVisibility(View.INVISIBLE);
		
		siegerbild = (ImageView)findViewById(R.id.siegerbild);
		siegerbild.setVisibility(View.INVISIBLE);
		
		
		
	}
	
	private void hideSiegerehrung() {
		// Ausblenden der Items für die Siegerehrung
		
		pokal.setVisibility(View.INVISIBLE);
		
		pokal_text.setVisibility(View.INVISIBLE);
		
		siegerbild.setVisibility(View.INVISIBLE);
			
	}
	
	private void showSiegerehrung() {
		// Einblenden der Items für die Siegerehrung
	
		
		siegerbild.setImageBitmap(platz[0].getbitmap()); //Siegerbild mit Platz1 aus Liste beschreiben
		pokal_text.setText(Long.toString(platz[0].getzeit())); //Zeit von Platz eins zuordenen
		
		pokal.setVisibility(View.VISIBLE);
		
		pokal_text.setVisibility(View.VISIBLE);
		
		siegerbild.setVisibility(View.VISIBLE);
		
		spielbutton.setVisibility(View.VISIBLE);
		rundenzaehler=0;// Rundenzähler rücksetzen
		keepRunning=false; //Thread stoppen
		
		// zur Diagnose
		while (bildanzeigen.isAlive()){
			System.out.println("Is alive"); 
			
		}
		resetHighScore(); //HighScore für neues Spiel zurücksetzen
		
	}
	
	
	


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		// Bild von Kamera Activiy empfangen 
	    if( requestCode == 1337)
	    {
	    	System.out.println("Antwort von Kamera empfangen");
	    //  data.getExtras();
	    	//Zähler Spieler erhöhen
	    	spieleranzahl=spieleranzahl+1;
	    	
	    	//SpielButton einblenden wenn mehr als 2 Fotos aufgenommen
	    	if(spieleranzahl>=2){
				spielbutton.setVisibility(View.VISIBLE);
			}	
	    	//aktuelle Spieleranzahlanzeige aktualisieren
	    	anzahlspieler.setText(Integer.toString(spieleranzahl));
			
	    	//Antwort von Camera in Variable ablegen
	        thumbnail = (Bitmap) data.getExtras().get("data");
	 //       System.out.println("Thread: Pfad: "+ thumbnail.); 
	 		//Abspeichern aufrufen
	        storeImage(thumbnail);
	    }
	    else 
	    {
	        Toast.makeText(this, "Picture NOt taken", Toast.LENGTH_LONG).show();
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void storeImage(Bitmap image) {
	    File pictureFile = getOutputMediaFile();
	    if (pictureFile == null) {
	     //   Log.d(TAG,
	       //         "Error creating media file, check storage permissions: ");// e.getMessage());
	    	System.out.println("irgendwas stimmt da nicht"); 
			
	    	return;
	    } 
	    try {
	        FileOutputStream fos = new FileOutputStream(pictureFile);
	        image.compress(Bitmap.CompressFormat.PNG, 90, fos);
	        fos.close();
	        System.out.println("folgendes Foto abspeichern: " + pictureFile.getAbsolutePath()); 
			
	    } catch (FileNotFoundException e) {
	    	System.out.println("File not found: " + e.getMessage()); 
			
	  //      Log.d(TAG, "File not found: " + e.getMessage());
	    } catch (IOException e) {
	   //     Log.d(TAG, "Error accessing file: " + e.getMessage());
	    	System.out.println("Error accessing file " + e.getMessage()); 
			
	    }  
	}

	/** Create a File for saving an image or video */
	private  File getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this. 
	    File mediaStorageDir = filepfad; /*new File(Environment.getExternalStorageDirectory()
	            + "/Android/data/"
	            + getApplicationContext().getPackageName()
	            + "/Files"); */
	    
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            return null;
	        }
	    } 
	    // Create a media file name
	    File mediaFile;
	    //Bildname zusammenbauen (1.jpg usw) und abspeichern
	        String mImageName= String.valueOf(spieleranzahl) +".jpg";
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);  
	    return mediaFile;
	} 
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.touchpic, menu);
		return true;
	}
	
	
	public void onClick(View v) {
		
			
	}	
	
	
	public void Foto(View v){
		// wird beim Klick auf Foto Button aufgerufen
		
		// Kamera Activity aufrufen und Foto machen
		startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
		
    
		
		
	}
	public void Spiel(View v){
		//wird aufgerufen wenn der Spiel Button gedrückt wird
		System.out.println("Spiel gestartet");
		
		
		//Variable dass das Spiel gestartet ist, dient auch dazu um den Thread beim schliessen zu beenden
		keepRunning=true;
		
		pokal.setVisibility(View.INVISIBLE);
		
		pokal_text.setVisibility(View.INVISIBLE);
		
		siegerbild.setVisibility(View.INVISIBLE);
		
		bildanzeigen = new Thread(new Runnable() {
			  @Override
			  public void run() {
			    try {
			     
				while (keepRunning){
			    	
			    	Thread.sleep(1000);
			    	
			    	//Zufallsbild auswählen
			    	java.util.Random random = new java.util.Random();
			 		int bildnummer =1+ random.nextInt(spieleranzahl); 
			 		myJpgPath = pfad + "/"+bildnummer+".jpg";
			 		System.out.println("Thread: Pfad: "+ myJpgPath); 
			 		
			 		rundenzaehler++;
		
			 		//Start Timer für Reaktionszeit
			 		startTime = System.currentTimeMillis();
			 	
			    	//hier wird das anzuzeigende Bild dem MainThread übergeben, da nur dieser Bilder anzeigen kann
			    	bild.post(new Runnable(){
			    		 @Override
	                     public void run() {
			    			 //Zufallsbild dem Imagview zuordnen und anzeigen
			    			 bm = BitmapFactory.decodeFile(myJpgPath, options);
			    //			 TEST
			    			 
			    			 //Bitmap bmSrc1 = ((BitmapDrawable)ivSrc.getDrawable()).getBitmap();
			    	//		 Bitmap bmSrc2 = bmSrc1.copy(bmSrc1.getConfig(), true);
			    			 Bitmap bmtemp = bm.copy(bm.getConfig(), true);
			                 bmsmall=Bitmap.createScaledBitmap(bmtemp, 60, 60, true); 
			    			 
			    			 bild.setImageBitmap(bm);
	                         //Highscore ausblenden
	                         HideHighScore();
	                         //Bild einblenden
	                         if(rundenzaehler>=(spieleranzahl*5))
	                         {
	                         
	                        	showSiegerehrung(); //Spiel zu ende Siegerehrung aufrufen
	                        
	                         }
	                         else{ 
	                        	 bild.setVisibility(View.VISIBLE);
	                         }
			    		 }
			    	});
			    	
			    	if(rundenzaehler>=(spieleranzahl*5))
                    {
			    		System.out.println("break"); 
				 		
			    		break; //Siegerehrung aktiv-> Thread beenden
                    }
			    	Thread.sleep(5000);
			      }
				System.out.println("Thread beendet"); 
		 		
			    } catch (Exception e) {
			      e.getLocalizedMessage();
			      System.out.println("Thread: Exception"); 
			 		
			    }
			  }
			});
		
		//Thread starten
		bildanzeigen.start();
		
		//alle anderen Buttons und Text ausblenden
		fotobutton.setVisibility(View.INVISIBLE);
		spielbutton.setVisibility(View.INVISIBLE);
		anzahlspieler.setVisibility(View.INVISIBLE);
		anzahlspielertext.setVisibility(View.INVISIBLE);

	}
	public void BildKlick(View v) {
		//Wird aufgerufen wenn das Bild angeklickt wird(Listener in xml definiert)
		
  	 if (keepRunning==true){
   	  //nur wenn Spiel schon läuft ausführen
  		 
		    switch (v.getId()) {
		    //ist unser Bild angeklickt? (bei nur einem Bild Listener nicht nötig)
		      case R.id.bild:
		    	  System.out.println("Reaktionszeit: "+(System.currentTimeMillis()-startTime));
		    	  bild.setVisibility(View.INVISIBLE); //Bild ausblenden
		     
		    	  aktuelle.setzeit(System.currentTimeMillis()-startTime); //aktuelle Zeit abspeichern 
		    	  aktuelle.setfarbe(2); // Farbe auf grün setzen
		    	  aktuelle.setbitmap(bmsmall);
		    	  
		    	  //Farbe auf schwarz setzen
		    	  for(int i = 0; i < platz.length; i++){
			  			platz[i].setfarbe(1);
			  		}
		    	  
		    	  // wenn aktuelle Zeit kürzer wie Eintrag 10 dann aktuelle anstatt Eintrag 10 kopieren
		    	  if (aktuelle.getzeit()<platz[9].getzeit()){
		    //		  System.out.println("aktuell<platz9");
		    		  platz[9].setzeit(aktuelle.getzeit());
		    		  platz[9].setfarbe(aktuelle.getfarbe());
		    		platz[9].setbitmap(aktuelle.getbitmap()); 
		    	  }
		    	  


		     //System.out.println("vor Sortieren"+platz[9].getimage().);
		    	  
		    	  Arrays.sort(platz);
		    	//  System.out.println("sortieren fertig"+platz[9].getzeit());  
		    	 
		    	  
		    	  //platz1.setImageBitmap(bmsmall);
				    
		    	  ShowHighScore();
		        break;
		      
		      }
  	 		}
		}
	
	public void ShowHighScore(){
  	  
	//	System.out.println("showhighscore settext"+platz[0].getzeit());
		
		//nur aktuellen Eintrag rot färben
		platz1_text.setText(Long.toString(platz[0].getzeit()));
		if(platz[0].getfarbe()==2){
			platz1_text.setTextColor(Color.RED);
		}
		else
			platz1_text.setTextColor(Color.BLACK);
			
		platz2_text.setText(Long.toString(platz[1].getzeit()));
		if(platz[1].getfarbe()==2){
			platz2_text.setTextColor(Color.RED);
		}
		else
			platz2_text.setTextColor(Color.BLACK);
		
		platz3_text.setText(Long.toString(platz[2].getzeit()));
		if(platz[2].getfarbe()==2){
			platz3_text.setTextColor(Color.RED);
		}
		else
			platz3_text.setTextColor(Color.BLACK);
		
		platz4_text.setText(Long.toString(platz[3].getzeit()));
		if(platz[3].getfarbe()==2){
			platz4_text.setTextColor(Color.RED);
		}
		else
			platz4_text.setTextColor(Color.BLACK);
		
		platz5_text.setText(Long.toString(platz[4].getzeit()));
		if(platz[4].getfarbe()==2){
			platz5_text.setTextColor(Color.RED);
		}
		else
			platz5_text.setTextColor(Color.BLACK);
		
		platz6_text.setText(Long.toString(platz[5].getzeit()));
		if(platz[5].getfarbe()==2){
			platz6_text.setTextColor(Color.RED);
		}
		else
			platz6_text.setTextColor(Color.BLACK);
		
		platz7_text.setText(Long.toString(platz[6].getzeit()));
		if(platz[6].getfarbe()==2){
			platz7_text.setTextColor(Color.RED);
		}
		else
			platz7_text.setTextColor(Color.BLACK);
		
		platz8_text.setText(Long.toString(platz[7].getzeit()));
		if(platz[7].getfarbe()==2){
			platz8_text.setTextColor(Color.RED);
		}
		else
			platz8_text.setTextColor(Color.BLACK);
		
		platz9_text.setText(Long.toString(platz[8].getzeit()));
		if(platz[8].getfarbe()==2){
			platz9_text.setTextColor(Color.RED);
		}
		else
			platz9_text.setTextColor(Color.BLACK);
		
		platz10_text.setText(Long.toString(platz[9].getzeit()));
		if(platz[9].getfarbe()==2){
			platz10_text.setTextColor(Color.RED);
		}
		else
			platz10_text.setTextColor(Color.BLACK);
		
		//ImageView aus Liste beschreiben
		platz1.setImageBitmap(platz[0].getbitmap());
		platz2.setImageBitmap(platz[1].getbitmap());
		platz3.setImageBitmap(platz[2].getbitmap());
		platz4.setImageBitmap(platz[3].getbitmap());
		platz5.setImageBitmap(platz[4].getbitmap());
		platz6.setImageBitmap(platz[5].getbitmap());
		platz7.setImageBitmap(platz[6].getbitmap());
		platz8.setImageBitmap(platz[7].getbitmap());
		platz9.setImageBitmap(platz[8].getbitmap());
		platz10.setImageBitmap(platz[9].getbitmap());
	  	
		
		
	//	System.out.println("showhighscore setimage");
		
		
		
		//platz10=(platz[0].getimage());
		//RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(100, 100);
		//platz10.setLayoutParams(layoutParams);
		
		
	//	System.out.println("showhighscore nach setimage");
		
		//Bild ausblenden
		 bild.setVisibility(View.INVISIBLE);
		//Highscore Platzierungstext einblenden
		p1.setVisibility(View.VISIBLE);
		p2.setVisibility(View.VISIBLE);
		p3.setVisibility(View.VISIBLE);
		p4.setVisibility(View.VISIBLE);
		p5.setVisibility(View.VISIBLE);
		p6.setVisibility(View.VISIBLE);
		p7.setVisibility(View.VISIBLE);
		p8.setVisibility(View.VISIBLE);
		p9.setVisibility(View.VISIBLE);
		p10.setVisibility(View.VISIBLE);
		
		
		
		//Highscore PlatzierungsBild einblenden
		platz1.setVisibility(View.VISIBLE);
		platz2.setVisibility(View.VISIBLE);
		platz3.setVisibility(View.VISIBLE);
		platz4.setVisibility(View.VISIBLE);
		platz5.setVisibility(View.VISIBLE);
		platz6.setVisibility(View.VISIBLE);
		platz7.setVisibility(View.VISIBLE);
		platz8.setVisibility(View.VISIBLE);
		platz9.setVisibility(View.VISIBLE);
		platz10.setVisibility(View.VISIBLE);
	  	   
		//Highscore Platzierungszeit einblenden
		platz1_text.setVisibility(View.VISIBLE);
		platz2_text.setVisibility(View.VISIBLE);
		platz3_text.setVisibility(View.VISIBLE);
		platz4_text.setVisibility(View.VISIBLE);
		platz5_text.setVisibility(View.VISIBLE);
		platz6_text.setVisibility(View.VISIBLE);
		platz7_text.setVisibility(View.VISIBLE);
		platz8_text.setVisibility(View.VISIBLE);
		platz9_text.setVisibility(View.VISIBLE);
		platz10_text.setVisibility(View.VISIBLE);
	  		  	 			
	}

	public void HideHighScore(){
	  	  
		p1.setVisibility(View.INVISIBLE);
		p2.setVisibility(View.INVISIBLE);
		p3.setVisibility(View.INVISIBLE);
		p4.setVisibility(View.INVISIBLE);
		p5.setVisibility(View.INVISIBLE);
		p6.setVisibility(View.INVISIBLE);
		p7.setVisibility(View.INVISIBLE);
		p8.setVisibility(View.INVISIBLE);
		p9.setVisibility(View.INVISIBLE);
		p10.setVisibility(View.INVISIBLE);
		  
		platz1.setVisibility(View.INVISIBLE);
		platz2.setVisibility(View.INVISIBLE);
		platz3.setVisibility(View.INVISIBLE);
		platz4.setVisibility(View.INVISIBLE);
		platz5.setVisibility(View.INVISIBLE);
		platz6.setVisibility(View.INVISIBLE);
		platz7.setVisibility(View.INVISIBLE);
		platz8.setVisibility(View.INVISIBLE);
		platz9.setVisibility(View.INVISIBLE);
		platz10.setVisibility(View.INVISIBLE);
	  	    
		platz1_text.setVisibility(View.INVISIBLE);
		platz2_text.setVisibility(View.INVISIBLE);
		platz3_text.setVisibility(View.INVISIBLE);
		platz4_text.setVisibility(View.INVISIBLE);
		platz5_text.setVisibility(View.INVISIBLE);
		platz6_text.setVisibility(View.INVISIBLE);
		platz7_text.setVisibility(View.INVISIBLE);
		platz8_text.setVisibility(View.INVISIBLE);
		platz9_text.setVisibility(View.INVISIBLE);
		platz10_text.setVisibility(View.INVISIBLE);
	  		  	 			
	}

	public void resetHighScore(){
		
		// Alle zeiten auf 20000 zurücksetzen
		for(int i = 0; i < platz.length; i++){
			platz[i].setzeit(20000);
		}
	
		
	}
	
	  public void InitHighScore(){
	  		p1 = (TextView)findViewById(R.id.Platz1);
	  		p2 = (TextView)findViewById(R.id.Platz2);
	  		p3 = (TextView)findViewById(R.id.Platz3);
	  		p4 = (TextView)findViewById(R.id.Platz4);
	  		p5 = (TextView)findViewById(R.id.Platz5);
	  		p6 = (TextView)findViewById(R.id.Platz6);
	  		p7 = (TextView)findViewById(R.id.Platz7);
	  		p8 = (TextView)findViewById(R.id.Platz8);
	  		p9 = (TextView)findViewById(R.id.Platz9);
	  		p10 = (TextView)findViewById(R.id.Platz10);
	  		
	  		platz1= (ImageView)findViewById(R.id.imageView1);
	  		platz2= (ImageView)findViewById(R.id.imageView2);
	  		platz3= (ImageView)findViewById(R.id.imageView3);
	  		platz4= (ImageView)findViewById(R.id.imageView4);
	  		platz5= (ImageView)findViewById(R.id.imageView5);
	  		platz6= (ImageView)findViewById(R.id.imageView6);
	  		platz7= (ImageView)findViewById(R.id.imageView7);
	  		platz8= (ImageView)findViewById(R.id.imageView8);
	  		platz9= (ImageView)findViewById(R.id.imageView9);
	  		platz10= (ImageView)findViewById(R.id.imageView10);
	  	
	  		
	  		platz1_text = (TextView)findViewById(R.id.Platz1_text);
	  		platz2_text = (TextView)findViewById(R.id.Platz2_text);
	  		platz3_text = (TextView)findViewById(R.id.Platz3_text);
	  		platz4_text = (TextView)findViewById(R.id.Platz4_text);
	  		platz5_text = (TextView)findViewById(R.id.Platz5_text);
	  		platz6_text = (TextView)findViewById(R.id.Platz6_text);
	  		platz7_text = (TextView)findViewById(R.id.Platz7_text);
	  		platz8_text = (TextView)findViewById(R.id.Platz8_text);
	  		platz9_text = (TextView)findViewById(R.id.Platz9_text);
	  		platz10_text = (TextView)findViewById(R.id.Platz10_text);
			  	
	  	  System.out.println("Init Highscore");
	  	  
	  	  
	  		platz= new ListenEintrag[10]; //Highscore Objekte erzeugen
	  		aktuelle = new ListenEintrag(); // Objekt für Aktuelle Daten erzeugen 
	  		
	  		
	  		
	  		//Highscore einträge mit 20000ms initialisieren

	  		for(int i = 0; i < platz.length; i++){
	  			platz[i] = new ListenEintrag();
	  		}
	  		
	  		
	  	  }

	  
	  
	  @Override
	  public void onConfigurationChanged(Configuration newConfig) {
		  //wird aufgerufen wenn das deklarierte ereignis unter manifest configchanes auftritt
		  //hier confgichanges =orientation damit nicht oncreate bei lageänderung aufgerufen wird
	      super.onConfigurationChanged(newConfig);
	  }
	  
	  
	  @Override
		public void onPause(){
			super.onPause();
			keepRunning=false;
			 System.out.println("OnPause"); 
			 
		}

	  @Override
	  protected void onDestroy() {
	       
		  super.onDestroy();
		  keepRunning=false;
		  System.out.println("OnDestroy"); 
		 	
	      }


	  
}

class ListenEintrag implements Comparable<ListenEintrag>{

	private ImageView image;
	private long zeit=20000;
	private int farbe; //schwarz =1, grün =2
	private Bitmap bitmap;
	
	public Bitmap getbitmap(){
		return bitmap;
	}
	
	
	public ImageView getimage(){
		return image;
	}
	
	public long getzeit(){
		return zeit;
	}
	
	public int getfarbe(){
		return farbe;
	}
	
	public void setbitmap(Bitmap a){
		bitmap = a;
	}
	
	public void setimage(ImageView b){
		
	//	image = new ImageView();
		image = b;
	}
	
	public void setzeit(long z){
		zeit=z;
	}
	
	public void setfarbe(int f){
		farbe = f;
	}
	
	//wird beim Sortieren der Objekte aufgerufen
	public int compareTo(ListenEintrag obj){
		if(this.zeit< obj.zeit){
			return -1;
		}
		if(this.zeit> obj.zeit){
			return 1;
		}
		return 0;
	}
	
}
