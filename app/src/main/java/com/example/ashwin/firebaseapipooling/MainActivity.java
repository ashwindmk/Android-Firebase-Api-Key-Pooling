package com.example.ashwin.firebaseapipooling;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<ApiKey> apiKeyList; // = new ArrayList<>();
    private TextView mTextView, mCurrentKeyTextView;
    private EditText mApiKeyEditText;
    private String mCurrentKeyString = "";
    private boolean mFailBoolean = false;
    private int mTotalKeysCount = 0, mFailsCount = 0;
    private ApiKey mCurrentApiKeyObject = null;
    public static final int FAIL_VALUE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init()
    {
        mTextView = (TextView) findViewById(R.id.textView);
        mApiKeyEditText = (EditText) findViewById(R.id.apiKeyEditText);
        mCurrentKeyTextView = (TextView) findViewById(R.id.currentKeyTextView);

        initFirebaseDatabase();

        setCurrentKey();
    }

    private void initFirebaseDatabase()
    {
        DatabaseReference readRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://android-firebase-test-a2516.firebaseio.com/ApiPooling/");

        Query queryRef = readRef.orderByChild("rank");

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //string to store all the names and address
                String string = "";
                apiKeyList = new ArrayList<ApiKey>();

                for (DataSnapshot postSnapshot : snapshot.getChildren())
                {
                    //getting the data from snapshot
                    ApiKey apiKey = postSnapshot.getValue(ApiKey.class);

                    //adding to arraylist
                    apiKeyList.add(apiKey);

                    //adding it to a string
                    string += "\n\n" + apiKey.toString();
                }

                //displaying it on textview
                //mTextView.setText(string);

                String line = "";

                //get from arraylist
                for(ApiKey apiKey : apiKeyList)
                {
                    line = line + "Api Key: " + apiKey.getApi() + ", " + apiKey.getRank() + "\n\n";
                }

                mTextView.setText( line );

                mTotalKeysCount = apiKeyList.size();

                Log.w(TAG, "mFailBoolean : "+String.valueOf(mFailBoolean));

                if( mCurrentKeyString.equals("") || mFailBoolean)
                {
                    for( ApiKey apiKey : apiKeyList )
                    {
                        if( mFailsCount > mTotalKeysCount )
                        {
                            setAllFail();
                            break;
                        }
                        mCurrentApiKeyObject = apiKey;
                        mCurrentKeyString = mCurrentApiKeyObject.getApi();
                        if( mCurrentApiKeyObject.getRank() != FAIL_VALUE )
                        {
                            mFailBoolean = false;
                            setCurrentKey();
                            break;
                        }
                        else
                        {
                            mFailsCount += 1;
                            mFailBoolean = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "The read failed: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setCurrentKey()
    {
        Log.w(TAG, "setCurrentKey : mFailsCount : " + mFailsCount);

        if( mFailsCount <= mTotalKeysCount )
        {
            mCurrentKeyTextView.setText("current key : " + mCurrentKeyString);
        }
        else
        {
            setAllFail();
        }
    }


    private void setAllFail()
    {
        Toast.makeText(this, "All keys failed", Toast.LENGTH_LONG).show();
        mCurrentKeyTextView.setText("All keys fail");
    }


    public void setFail(View view)
    {
        if( mFailsCount <= mTotalKeysCount )
        {
            String api = mApiKeyEditText.getText().toString();
            int rank = FAIL_VALUE;

            ApiKey apiKey = new ApiKey(api, rank);


            if (api.equals(mCurrentKeyString))
            {
                mFailBoolean = true;
                mFailsCount += 1;
            }

            writeToDatabase(apiKey);
        }
        else
        {
            setAllFail();
        }
    }


    public void setSuccess(View view)
    {
        String api = (mApiKeyEditText.getText().toString()).trim();
        int rank = 1;

        ApiKey apiKey = new ApiKey(api, rank);

        mFailsCount = 0;

        writeToDatabase(apiKey);
    }


    private void writeToDatabase(final ApiKey apiKey) throws NoSuchElementException
    {
        final DatabaseReference writeRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://android-firebase-test-a2516.firebaseio.com/");

        Query query = writeRef.child("ApiPooling").orderByChild("api").equalTo(apiKey.getApi());
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.getChildren().iterator().hasNext())
                {
                    DataSnapshot nodeDataSnapshot = dataSnapshot.getChildren().iterator().next();
                    String key = nodeDataSnapshot.getKey(); //this key is like `K1NRz9l5PU_0CFDtgXz`
                    String path = "/" + dataSnapshot.getKey() + "/" + key;

                    //Log.d("MainActivity", "dataSnapshot.getKey() : " + dataSnapshot.getKey());
                    //Log.d("MainActivity", "dataSnapshot.getValue() : " + dataSnapshot.getValue());
                    //Log.d("MainActivity", "nodeDataSnapshot.getKey() : " + nodeDataSnapshot.getKey());
                    //Log.d("MainActivity", "nodeDataSnapshot.getValue() : " + nodeDataSnapshot.getValue());
                    //Log.d("MainActivity", "nodeDataSnapshot.getChildren() : " + nodeDataSnapshot.getChildren());

                    //get current value
                    ApiKey apiKeyClass = nodeDataSnapshot.getValue(ApiKey.class);
                    int currentRank = apiKeyClass.getRank();
                    if (apiKey.getRank() == FAIL_VALUE) {
                        //fail
                        currentRank = FAIL_VALUE;
                    } else {
                        //success
                        currentRank = (currentRank + 1) % 101; //+1
                    }

                    //set new rank value
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("rank", currentRank);

                    writeRef.child(path).updateChildren(result);
                }
                else
                {
                    Log.e(TAG, "No such element exception");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.e(TAG, "Error: " + "find onCancelled: " + databaseError);
            }
        });
    }


}
