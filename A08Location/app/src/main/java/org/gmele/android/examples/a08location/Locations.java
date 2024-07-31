package org.gmele.android.examples.a08location;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class Locations extends Activity implements OnCheckedChangeListener, LocationListener, OnClickListener
{
    TextView TvLine1;
    TextView TvLine2;
    TextView TvLine3;
    TextView TvLine4;
    Switch SwService;
    RadioGroup RgAccur;
    RadioGroup RgAlt;
    RadioGroup RgPow;
    RadioGroup RgEnable;
    Button BtAddr;
    EditText EtAddr;
    LocationManager LocMan;
    Location location;
    String Provider;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.locationlay);
        TvLine1 = (TextView) findViewById (R.id.TvLine1);
        TvLine2 = (TextView) findViewById (R.id.TvLine2);
        TvLine3 = (TextView) findViewById (R.id.TvLine3);
        TvLine4 = (TextView) findViewById (R.id.TvLine4);
        SwService = (Switch) findViewById (R.id.SwService);
        SwService.setOnCheckedChangeListener (this);
        RgAccur = (RadioGroup) findViewById (R.id.RgAccuracy);
        RgAlt = (RadioGroup) findViewById (R.id.RgAltitude);
        RgPow = (RadioGroup) findViewById (R.id.RgPower);
        RgEnable = (RadioGroup) findViewById (R.id.RgEnabled);
        BtAddr = (Button) findViewById (R.id.BtAddress);
        BtAddr.setOnClickListener (this);
        EtAddr = (EditText) findViewById (R.id.EtMulti);

    }


    @Override
    public void onCheckedChanged (CompoundButton buttonView, boolean isChecked)
    {
        System.out.println ("##### 1");
        //isActivated ()
        if (buttonView == SwService)
        {
            if (SwService.isChecked ())
            {
                System.out.println ("##### 1a");
                Activate ();
            }
            else
            {
                System.out.println ("##### 1b");
                DeActivate ();
            }
        }

    }

    private void Activate ()
    {
        System.out.println ("##### 3");
        LocMan = (LocationManager) getSystemService (Context.LOCATION_SERVICE);
        Criteria criteria = GetCriteria ();
        DebugAllProviders (LocMan);
        Provider = LocMan.getBestProvider (criteria, GetEnabled ());  //enabled only
        TvLine1.setText ("Provider: " + Provider);
        if (Provider != null)
        {
            location = LocMan.getLastKnownLocation (Provider);
            onLocationChanged (location);
            try
            {
                LocMan.requestLocationUpdates (Provider, 3000, 2.5f, this);  //Both??
                TvLine2.setText ("O.K.");
            }
            catch (RuntimeException e)
            {
                TvLine2.setText ("Error: " + e.getMessage () + " " + e.getClass ().getSimpleName ());
            }
        }

    }

    private void DeActivate ()
    {
        System.out.println ("##### 2");
        LocMan.removeUpdates (this);
    }

    private Criteria GetCriteria ()
    {
        Criteria tmp = new Criteria ();
        switch (RgAccur.getCheckedRadioButtonId ())
        {
            case R.id.RbAccurHigh: tmp.setAccuracy (Criteria.ACCURACY_HIGH); break;
            case R.id.RbAccurLow: tmp.setAccuracy (Criteria.ACCURACY_LOW); break;
            case R.id.RbAccurFine: tmp.setAccuracy (Criteria.ACCURACY_FINE); break;
            case R.id.RbAccurCoarse: tmp.setAccuracy (Criteria.ACCURACY_COARSE); break;
        }
        if (RgAlt.getCheckedRadioButtonId () == R.id.RbAltYes)
            tmp.setAltitudeRequired (true);
        else
            tmp.setAltitudeRequired (false);
        if (RgPow.getCheckedRadioButtonId () == R.id.RbPowtHigh)
            tmp.setPowerRequirement (Criteria.POWER_HIGH);
        else
            tmp.setPowerRequirement (Criteria.POWER_LOW);
        return tmp;
    }

    private boolean GetEnabled ()
    {
        if (RgEnable.getCheckedRadioButtonId () == R.id.RbEnaYes)
            return true;
        else
            return false;
    }



    @Override
    public void onLocationChanged (Location location)
    {
        if (location == null)
        {
            TvLine3.setText ("---");
            TvLine4.setText ("---");
            return;
        }
        this.location = location;
        TvLine3.setText ("Lat: " + location.getLatitude () + "   Lon: " + location.getLongitude ());
        TvLine4.setText ("Alt: " + location.getAltitude () + "   Bea:" + location.getBearing ());
        System.out.println ("!!!Updated");
    }

    @Override
    public void onStatusChanged (String provider, int status, Bundle extras)
    {
        System.out.println ("##### 6");
        switch (status)
        {
            case LocationProvider.OUT_OF_SERVICE:
                TvLine2.setText ("Out of Service");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                TvLine2.setText ("Temporarily UnAvailable");
                break;
            case LocationProvider.AVAILABLE:
                TvLine2.setText ("On Line");
                break;
        }
    }

    @Override
    public void onProviderEnabled (String provider)
    {
        System.out.println ("##### 7");
        TvLine2.setText ("Provider " + provider + " enabled");
    }

    @Override
    public void onProviderDisabled (String provider)
    {
        System.out.println ("##### 8");
        TvLine2.setText ("Provider " + provider + " disabled");
    }

    @Override
    public void onBackPressed ()
    {
        finish ();
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy ();
        System.out.println ("##### End... ");
    }

    @Override
    public void onClick (View v)
    {
        if (v == BtAddr && location != null)
        {
            GeoThread.start ();
        }
    }

    Thread GeoThread = new Thread ()
    {
        String Result;

        public void run ()
        {
            List <Address> addressList = null;
            Geocoder geocoder = new Geocoder (Locations.this, Locale.getDefault ());
            try
            {
                addressList = geocoder.getFromLocation (location.getLatitude (), location.getLongitude (), 1);
            }
            catch (IOException e)
            {
                System.out.println ("IOException: " + e.getMessage ());
            }
            if (addressList != null && addressList.size() > 0)
            {
                Address Addr = addressList.get (0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Addr.getMaxAddressLineIndex (); i++)
                {
                    sb.append (Addr.getAddressLine (i)). append("\n");
                }
                sb.append(Addr.getLocality()).append("\n");
                sb.append(Addr.getPostalCode()).append("\n");
                sb.append(Addr.getCountryName()).append ("\n");
                sb.append(Addr.getPhone ());
                Result = sb.toString();
            }
            else
            {
                Result = "Χωρίς διεύθυνση";
            }
            runOnUiThread (new Runnable ()
            {
                public void run ()
                {
                    EtAddr.setText (Result);
                }
            });
        }
    };

    void DebugAllProviders (LocationManager LM)
    {
        List <String> AllPr = LM.getAllProviders ();
        for (String tmp: AllPr)
            System.out.println ("!!!" + tmp);
    }
}
