package com.pm.library.controllers.fragments;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.pm.library.BuildConfig;
import com.pm.library.R;
import com.pm.library.business.api.PlaceResult;
import com.pm.library.business.api.PlacesResponse;
import com.pm.library.business.api.interfaces.IGooglePlacesApiService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsFragment extends Fragment {

    FusedLocationProviderClient fusedLocationProviderClient; //decide si usar gps o wifi
    GoogleMap gMap;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getUserLocationAndSearchBookstores();
                } else {
                    Toast.makeText(requireContext(), "Permiso de ubicacion denegado", Toast.LENGTH_SHORT).show();
                }
            });

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            gMap = googleMap;
            getUserLocationAndSearchBookstores();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback); //prepara el mapa en segundo plano
        }
    }

    private void getUserLocationAndSearchBookstores() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        //activamos el punto de mi ubicacion actual
        gMap.setMyLocationEnabled(true);

        //obtenemos la ultima ubicacion conocida
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            LatLng userLocation;
            if (location != null) {
                Log.d("MAPS", "Ubicación obtenida");
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14));
                searchBookstores(userLocation);
            } else {
                Toast.makeText(requireContext(), "Hubo un problema al detectar su ubicación.", Toast.LENGTH_LONG).show();
                Log.d("MAPS", "Ubicación NO obtenida");
            }
        });
    }

    private void searchBookstores(LatLng userLocation) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IGooglePlacesApiService service = retrofit.create(IGooglePlacesApiService.class);
        String location = userLocation.latitude + "," + userLocation.longitude;
        service.getNearbyBookstores(location,3000, "book_store", "libreria", BuildConfig.GOOGLE_MAPS_API_KEY)
                .enqueue(new Callback<PlacesResponse>() {
                    @Override
                    public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            requireActivity().runOnUiThread(() -> {
                                gMap.clear();
                                List<PlaceResult> results = response.body().results;
                                for (int i = 0; i < results.size() && i < 6; i++) {
                                    PlaceResult result = results.get(i);
                                    LatLng position = new LatLng(result.geometry.location.lat, result.geometry.location.lng);
                                    gMap.addMarker(new MarkerOptions()
                                            .position(position)
                                            .title(result.name)
                                            .snippet(result.vicinity));
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), "Error al obtener resultados", Toast.LENGTH_SHORT).show();
                            Log.e("RETROFIT_MAPS", "Error en respuesta: " + response.code());
                            addSampleBookStores();
                        }
                    }

                    @Override
                    public void onFailure(Call<PlacesResponse> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error al obtener resultados", Toast.LENGTH_SHORT).show();
                        Log.e("RETROFIT_MAPS", "Fallo: " + t.getMessage());
                        addSampleBookStores();
                    }
                });
    }


    private void addSampleBookStores() {
        gMap.addMarker(new MarkerOptions()
                .position(new LatLng(-32.8895, -68.8458))
                .title("Librería El Ateneo")
                .snippet("Centro, Mendoza"));

        gMap.addMarker(new MarkerOptions()
                .position(new LatLng(-32.8908, -68.8272))
                .title("Librería Rayuela")
                .snippet("Mendoza"));
    }
}