package com.example.usersasbeacons;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "uab";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 0x0005;
    private Button sendButton;
    private Button clearButton;
    private EditText sendText;
    private TextView receivedText;
    private User user;
    private String userKey;

    public String masterUUID;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveUsers();


        sendButton = findViewById(R.id.buttonSend);
        clearButton = findViewById(R.id.buttonClear);

        Log.d("demo", UUID.randomUUID().toString());

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receivedText.setText("");
                receivedText.setHint("Waiting to receive");
            }
        });

    }

    public void saveUsers(){
        FirebaseFirestore saveDB = FirebaseFirestore.getInstance();

        //User 1...
        User user1 = new User("user1","Beaconing user 1", Identifier.parse(UUID.randomUUID().toString()).toString());


        saveDB.collection("users").add(user1.toHashMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        MainActivity.this.userKey = documentReference.getId();
                        getMasterID();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Couldn't save userss");
                    }
                });

    }

    public void getMasterID(){

        //Initializing Firebase...
        db = FirebaseFirestore.getInstance();

        //Getting master key...
        db.collection("uABmaster").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){

                            List<DocumentSnapshot> masterDocument = task.getResult().getDocuments();

                            String masterKey = (String) masterDocument.get(0).getData().get("masterKey");

                            Identifier masterID = Identifier.parse(masterKey);
                            Log.d(TAG, "masterKey: "+ masterKey+" masterID: "+masterID);
                            MainActivity.this.masterUUID = masterID.toString();
                            getUserDetails();
                            //transmitBeacon(masterKey);

//                            for(QueryDocumentSnapshot document :task.getResult()){
//                                Log.d(TAG, document.getId() + " => "+document.getData());
//                            }
                            getUserDetails();
                        } else{
                            Log.d(TAG, "Failed getting documents: "+ task.getException());
                        }
                    }
                });
    }

    public void getUserDetails(){
        DocumentReference documentReference = db.collection("users").document(userKey);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()){
                        Map<String, Object> userReceived = documentSnapshot.getData();
                        MainActivity.this.user = new User(userReceived.get("name").toString(),userReceived.get("message").toString(),userReceived.get("instanceID").toString());
                        transmitBeacon(MainActivity.this.user.getInstanceID());
                    } else {
                        Log.d(TAG, "get failed with reading from Firebase", task.getException());
                    }
                }else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void transmitBeacon(String instanceKey){
        final String instanceUUID = instanceKey;
        Beacon beacon = new Beacon.Builder()
                .setId1(this.masterUUID)
                .setId2(instanceUUID)
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[] {0l}))
                .build();

        BeaconParser beaconParser = new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT);
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.e(TAG, "Advertisement start succeeded with ID: "+instanceUUID);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.e(TAG, "Advertisement start failed with code: "+errorCode);
            }
        });
    }


}
