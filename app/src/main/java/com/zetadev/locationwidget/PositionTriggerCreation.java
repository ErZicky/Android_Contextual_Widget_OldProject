package com.zetadev.locationwidget;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

public class PositionTriggerCreation extends AppCompatActivity {
    private MapView map = null;
    Button getPinLocationButton;
    EditText AddressText;
    EditText CoordinateText;
    IMapController mapController;
    ImageView setAddIcon;
    ImageView setCordsIcon;
    TextView OSMcredits;
    SeekBar radiusSlider;
     Polygon circlePolygon;
     TextView radiusText;
     Button ConfirmBtn;
    MyLocationNewOverlay mLocationOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_position_trigger_creation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(this.getPackageName());

        map = (MapView) findViewById(R.id.Mainmap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        CoordinateText = findViewById(R.id.CoordinateText);
        AddressText = findViewById(R.id.AddressText);
        getPinLocationButton = findViewById(R.id.get_position_button);
        setAddIcon = findViewById(R.id.set_address_icon);
        setCordsIcon = findViewById(R.id.set_cords_icon);
        OSMcredits = findViewById(R.id.credittext);
        radiusSlider = findViewById(R.id.radiusSlider);
        radiusText = findViewById(R.id.radiuslabel);
        ConfirmBtn = findViewById(R.id.confirmButton);

        SetDefaultZoomPosition();
      //  ManageOSMCredit();

        AddressText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        AddressText.setRawInputType(InputType.TYPE_CLASS_TEXT);

        getPinLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPoint centerPoint = (GeoPoint) map.getMapCenter();
                double latitude = centerPoint.getLatitude();
                double longitude = centerPoint.getLongitude();

                CoordinateText.setText(latitude +", " + longitude);
                AddressText.setText(getAddressFromCoordinate( latitude, longitude));

                ConfirmBtn.setVisibility(View.VISIBLE);
            }
        });


        setAddIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AddressText.getText() != null)
                {
                    AddressUsed();
                }
            }
        });

        setCordsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CoordinateText.getText() != null)
                {
                    CoordinateUsed();
                }
            }
        });

        AddressText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    // Qui puoi gestire l'evento di invio

                    if(AddressText.getText() != null)
                    {
                        AddressUsed();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(AddressText.getWindowToken(), 0);
                        return true; // Indica che l'evento è stato gestito
                    }
                    else
                    {
                        return false;
                    }


                }
                return false; // Per altri actionId
            }
        });

        CoordinateText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    // Qui puoi gestire l'evento di invio

                    if(CoordinateText.getText() != null)
                    {
                        CoordinateUsed();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(CoordinateText.getWindowToken(), 0);
                        return true; // Indica che l'evento è stato gestito
                    }
                    else
                    {
                        return false;
                    }


                }


                return false;
            }
        });

        OSMcredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/copyright"));
                startActivity(browserIntent);
            }
        });




        radiusSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radius = Math.max(progress, 20);  // Assicura un raggio minimo di 20 metri
                updateCircleOnMap(radius);

                radiusText.setText( "Minimum distance for activation: " +radius + " mt");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Non necessario
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Non necessario
            }
        });



        ConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(PositionTriggerCreation.this, TriggerCreationActivity.class);
                String type = "location";
                i.putExtra("STRING_TYPE", type);
                i.putExtra("STRING_DEVICE", CoordinateText.getText().toString());
                i.putExtra("RADIUS_VALUE", radiusSlider.getProgress());
                startActivity(i);

            }
        });









    }



    private void updateCircleOnMap(int radius) {
        if (circlePolygon != null) {
            map.getOverlays().remove(circlePolygon); // Rimuove la vecchia circonferenza
        }





        // Crea una nuova Polygon per la circonferenza
        circlePolygon = new Polygon();
        circlePolygon.setFillColor(ColorsHelper.getSecondarycolor(this, 0.2f)); // Colore di riempimento trasparente
        circlePolygon.setStrokeColor(ColorsHelper.getPrimaryDarkcolor(this, 1)); // Colore del bordo
        circlePolygon.setStrokeWidth(2);

        // Aggiungi punti alla circonferenza
        List<GeoPoint> circlePoints = Polygon.pointsAsCircle((GeoPoint) map.getMapCenter(), radius);
        circlePolygon.setPoints(circlePoints);

        // Aggiungi la circonferenza alla mappa
        map.getOverlays().add(circlePolygon);
        map.invalidate(); // Rinfresca la mappa
    }






    private void AddressUsed()
    {

        LatLng cords = getCoordinateFromAddress(AddressText.getText().toString());

        if(cords != null)
        {
            CoordinateText.setText(cords.latitude +", " + cords.longitude);
            GeoPoint inputpoint =  new GeoPoint(cords.latitude, cords.longitude);
            mapController.setCenter(inputpoint);
            updateCircleOnMap(radiusSlider.getProgress());
            ConfirmBtn.setVisibility(View.VISIBLE);
            AddressText.setBackgroundTintList(null);
            CoordinateText.setBackgroundTintList(null);
        }
        else
        {
            AddressText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            CoordinateText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            Toast.makeText(this, "Could't find the address check again", Toast.LENGTH_SHORT).show();
        }


    }

    private void CoordinateUsed()
    {
        String[] cords = CoordinateText.getText().toString().split(",");

        String address = getAddressFromCoordinate(Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));
        if(address != null)
        {
            AddressText.setText(address);

            GeoPoint inputpoint =  new GeoPoint(Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));
            mapController.setCenter(inputpoint);
            updateCircleOnMap(radiusSlider.getProgress());
            ConfirmBtn.setVisibility(View.VISIBLE);
            AddressText.setBackgroundTintList(null);
            CoordinateText.setBackgroundTintList(null);

        }
        else
        {
            AddressText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            CoordinateText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            Toast.makeText(this, "Could't find the coordinates check again", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }


    private void SetDefaultZoomPosition()
    {
        //default starting position
       // map.setBuiltInZoomControls(false);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER); //stessa cosa di map.setBuiltInZoomControls(false)

        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(18);
        GeoPoint startPoint =  new GeoPoint(41.89488879836567, 12.482969250876316);
        mapController.setCenter(startPoint);
        updateCircleOnMap(radiusSlider.getProgress());

       CreateLocationOverlay();
    }


    private void CreateLocationOverlay()
    {
        //my position overlay
        GpsMyLocationProvider gps = new GpsMyLocationProvider(this);
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(gps,map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);


        //default starting position
        IMapController mapController = map.getController();


        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GeoPoint updatedStartPoint = new GeoPoint(gps.getLastKnownLocation());
                        mapController.setCenter(updatedStartPoint);
                        mapController.setZoom(18f);
                        updateCircleOnMap(radiusSlider.getProgress());
                    }
                });
            }
        });


    }

    private LatLng getCoordinateFromAddress(String addresstext)
    {
        LatLng convertedaddress =  AddressHelper.getCoordinatesFromAddress(this, addresstext);
        return convertedaddress;
    }

    private String getAddressFromCoordinate(Double latitude, Double longitude)
    {
        String convertedcoordinates =  AddressHelper.getAddressFromCoordinates(this, latitude, longitude);
        return convertedcoordinates;
    }
}