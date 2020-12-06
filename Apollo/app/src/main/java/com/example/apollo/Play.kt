package com.example.apollo

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import java.util.*
import kotlin.math.roundToInt

class Play : AppCompatActivity() {

    private lateinit var instructionTextView: TextView
    private lateinit var backgroundLayout: ConstraintLayout
    private lateinit var mHandler: Handler
    private lateinit var prefs: SharedPreferences
    // list of the last ten results, index 0 is oldest, index 10 is the newest
    private lateinit var fixedSizeList : ArrayList<LinkedTreeMap<String, Double>>

    private var finished = false
    private var waiting = true
    private var tap = false

    private var currentTime = 0L
    private var currIteration = 0
    private var currTrial = 0

    private val resultsMap = HashMap<String, Float>()
    private val booleanList = ArrayList<Boolean>()
    private val gson = Gson()

    private var goTotal = 0
    private var nogoTotal= 0
    private var goCount = 0
    private var nogoCount = 0
    private var goAccuracy = 0f
    private var nogoAccuracy = 0f
    private var goAvgSpeed = 0f
    private var goTotalTime = 0f

    // configurable stuff
    private val waitTime = 1000
    private val transitionTime = 1500
    private val tapPersistTime = 1500
    private val iterations = 10

    // audio systems
    private lateinit var soundPool : SoundPool // for button sound effects

    // sound file
    private var successSound : Int = 0
    private var failSound : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play)
        // hide title bar
        supportActionBar!!.hide()

        // setup audio systems
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        // initialize soundPool
        soundPool = SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build()

        // load sound effects
        successSound = soundPool.load(this, R.raw.success, 1)
        failSound = soundPool.load(this, R.raw.failure, 1)

        // debugging purposes, clears the sharedpreference file
        // clearPreferences()
        // val temp = getResultsList()
        // randomize the iterations of the game
        for(i in 0 until iterations) {
            // if i is even, then 'go'
            if(i % 2 == 0) {
                booleanList.add(true)
                goTotal++

            } else {
                // if i is odd, then 'no go'
                booleanList.add(false)
                nogoTotal++
                nogoCount++
            }
        }

        booleanList.shuffle()

        // get a reference to the shared preference file
        prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        // get the current trial
        currTrial = prefs.getInt("currTrial", 0)
        Log.i(TAG, currTrial.toString())

        // gets the list
        fixedSizeList = if(!prefs.contains("lastTen")) {
            ArrayList<LinkedTreeMap<String, Double>>()
        } else {
            val tempStr = prefs.getString("lastTen", "")
            gson.fromJson(tempStr, ArrayList<LinkedTreeMap<String, Double>>().javaClass)
        }

        // set up the initial wait screen
        instructionTextView = findViewById(R.id.instruction_text)
        backgroundLayout = findViewById(R.id.background_layout)
        backgroundLayout.setBackgroundResource(R.color.dark_purple)
        instructionTextView.setTextColor(Color.WHITE)
        instructionTextView.text = getString(R.string.wait_string)

        // set up onClickListener for the Constraint Layout
        backgroundLayout.setOnClickListener{
            if(finished) {
                // startActivity(Intent(applicationContext, Menu::class.java))
                finish()

            } else {
                // whether it is time to tap or not, record the data
                if(!waiting) {
                    waiting = true

                    // remove the oldest result
                    if(fixedSizeList.size == 10) {
                        fixedSizeList.removeAt(0)
                    }

                    if (tap) {
                        // play success sound effect
                        soundPool.play(successSound,0.5f,0.5f, 1, 0, 1.0f)

                        mHandler.removeCallbacksAndMessages(null);
                        goCount++

                        // to get the reaction time in seconds
                        val diffTime = (System.currentTimeMillis() - currentTime) / 1000f
                        goTotalTime += diffTime
                        Log.i(TAG, "clicked within $diffTime s")

                        // add a new result
                        val tempMap = LinkedTreeMap<String, Double>()
                        // 1f is true, 0f is false
                        tempMap["Go"] = 1.0
                        // stores the goSpeed
                        tempMap["goSpeed"] = diffTime * 1000.0
                        // 1f is true, 0f is false
                        tempMap["Success"] = 1.0
                        fixedSizeList.add(tempMap)

                        // if currIteration is greater
                        if(currIteration >= iterations) {
                            finishScreen()
                        } else {
                            backgroundLayout.setBackgroundResource(R.color.dark_purple)
                            instructionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
                            instructionTextView.text = getString(R.string.wait_string)
                            waitScreen()
                        }

                    } else {
                        // play fail sound effect
                        soundPool.play(failSound,0.5f,0.5f, 1, 0, 1.0f)

                        // add a new result
                        val tempMap = LinkedTreeMap<String, Double>()
                        // 1f is true, 0f is false
                        tempMap["Go"] = 0.0
                        // 1f is true, 0f is false
                        tempMap["Success"] = 0.0
                        fixedSizeList.add(tempMap)

                        mHandler.removeCallbacksAndMessages(null);
                        nogoCount--
                        backgroundLayout.setBackgroundResource(R.color.orange)
                        instructionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
                        instructionTextView.text = getString(R.string.failure)
                        transitionToWait()
                    }
                }
            }
        }

        // get the Handler
        mHandler = Handler(Looper.getMainLooper())
        // loads initial wait screen
        waitScreen()
    }

    private fun waitScreen() {
        // run the code after waitTime milliseconds
        mHandler.postDelayed({
            waitTimeOver()
        }, waitTime.toLong())
    }

    private fun waitTimeOver() {
        // When tap is true, Go, otherwise No-Go
        tap = booleanList[currIteration]
        currIteration += 1
        waiting = false
        // update the UI to Tap screen
        if(tap) {
            currentTime = System.currentTimeMillis()
            backgroundLayout.setBackgroundResource(R.color.light_blue)
            instructionTextView.setTextColor(ContextCompat.getColor(this, R.color.dark_purple))
            instructionTextView.text = getString(R.string.tap)
            transitionToFail()
        } else {
            backgroundLayout.setBackgroundResource(R.color.pink)
            instructionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            instructionTextView.text = getString(R.string.dont_tap)
            transitionToWaitNoGo()
        }
    }

    private fun transitionToWait() {
        // if currIteration is greater
        if(currIteration >= iterations) {
            transitionToFinished()
            return
        }

        mHandler.postDelayed({
            // Log.i(TAG, "loading wait screen"))
            backgroundLayout.setBackgroundResource(R.color.dark_purple)
            instructionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            instructionTextView.text = getString(R.string.wait_string)
            waiting = true
            waitScreen()
        }, transitionTime.toLong())
    }
    private fun transitionToWaitNoGo() {
        // remove the oldest result
        if(fixedSizeList.size == 10) {
            fixedSizeList.removeAt(0)
        }

        // if currIteration is greater
        if(currIteration >= iterations) {
            transitionToFinishedNoGo()
            return
        }

        mHandler.postDelayed({
            // Log.i(TAG, "loading wait screen"))
            soundPool.play(successSound,0.5f,0.5f, 1, 0, 1.0f)

            // add a new result
            val tempMap = LinkedTreeMap<String, Double>()
            // 1f is true, 0f is false
            tempMap["Go"] = 0.0
            // 1f is true, 0f is false
            tempMap["Success"] = 1.0
            fixedSizeList.add(tempMap)

            backgroundLayout.setBackgroundResource(R.color.dark_purple)
            instructionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            instructionTextView.text = getString(R.string.wait_string)
            waiting = true
            waitScreen()
        }, transitionTime.toLong())
    }

    private fun transitionToFail() {
        mHandler.postDelayed({
            waiting = true

            // Log.i(TAG, "loading failed screen")

            // play fail sound effect
            soundPool.play(failSound,0.5f,0.5f, 1, 0, 1.0f)

            // remove the oldest result
            if(fixedSizeList.size == 10) {
                fixedSizeList.removeAt(0)
            }

            // add a new result
            val tempMap = LinkedTreeMap<String, Double>()
            // 1f is true, 0f is false
            tempMap["Go"] = 1.0
            // 1f is true, 0f is false
            tempMap["Success"] = 0.0
            fixedSizeList.add(tempMap)

            backgroundLayout.setBackgroundResource(R.color.orange)
            instructionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            instructionTextView.text = getString(R.string.failure_to_tap)
            transitionToWait()
        }, tapPersistTime.toLong())
    }

    private fun transitionToFinished() {
        mHandler.postDelayed({
            finishScreen()
        }, transitionTime.toLong())
    }

    private fun transitionToFinishedNoGo() {
        mHandler.postDelayed({
            soundPool.play(successSound,0.5f,0.5f, 1, 0, 1.0f)

            // add a new result
            val tempMap = LinkedTreeMap<String, Double>()
            // 1f is true, 0f is false
            tempMap["Go"] = 0.0
            // 1f is true, 0f is false
            tempMap["Success"] = 1.0
            fixedSizeList.add(tempMap)

            finishScreen()
        }, transitionTime.toLong())
    }

    private fun finishScreen() {

        goAccuracy = goCount * 100f / goTotal
        nogoAccuracy = nogoCount * 100f / nogoTotal
        goAvgSpeed = (goTotalTime * 1000f / goCount)
        // this case only happens if the user misses all go's
        goAvgSpeed = if(goAvgSpeed.isNaN()) {
            0f
        } else {
            goAvgSpeed.roundToInt() / 1000f
        }

        val prefsEditor = prefs.edit()
        prefsEditor.putInt("currTrial", currTrial + 1)
        waiting = true
        finished = true

        //Log.i(TAG, "loading finish screen")

        Log.i(TAG, "goAccuracy = $goAccuracy\nnogoAccuracy = $nogoAccuracy\ngoAvgSpeed = $goAvgSpeed s" +
                "\ngoSuccesses = $goCount\nnogoSuccesses = $nogoCount")

//        getResults()
        // individual trials being stored
        resultsMap["goAccuracy"] = goAccuracy
        resultsMap["nogoAccuracy"] = nogoAccuracy
        resultsMap["goAvgSpeed"] = goAvgSpeed
        resultsMap["goSuccesses"] = goCount.toFloat()
        resultsMap["nogoSuccesses"] = nogoCount.toFloat()
        prefsEditor.putString("Trial$currTrial", gson.toJson(resultsMap))
        prefsEditor.putString("lastTen", gson.toJson(fixedSizeList))

        // calculate and update the totals
        var totalGoAccuracy = prefs.getFloat("totalGoAccuracy", 0f)
        var totalNogoAccuracy = prefs.getFloat("totalNogoAccuracy", 0f)
        var totalGoAvgSpeed = prefs.getFloat("totalGoAvgSpeed", 0f)
        var totalGoSuccesses = prefs.getFloat("totalGoSuccesses", 0f)
        var totalNogoSuccesses = prefs.getFloat("totalNogoSuccesses", 0f)

        totalGoAccuracy =
            ( (totalGoAccuracy * currTrial ) + goAccuracy) / (currTrial + 1)

        totalNogoAccuracy =
            ( (totalNogoAccuracy * currTrial ) + nogoAccuracy) / (currTrial + 1)

        totalGoAvgSpeed = if(goAvgSpeed == 0f) {
            totalGoAvgSpeed
        } else {
            if(totalGoAvgSpeed == 0f) {
                goAvgSpeed
            } else {
                ( (totalGoAvgSpeed * currTrial ) + goAvgSpeed) / (currTrial + 1)
            }
        }

        totalGoSuccesses += goCount
        totalNogoSuccesses += nogoCount

        prefsEditor.putFloat("totalGoAccuracy", totalGoAccuracy)
        prefsEditor.putFloat("totalNogoAccuracy", totalNogoAccuracy)
        prefsEditor.putFloat("totalGoAvgSpeed", totalGoAvgSpeed)
        prefsEditor.putFloat("totalGoSuccesses", totalGoSuccesses)
        prefsEditor.putFloat("totalNogoSuccesses", totalNogoSuccesses)

        Log.i(TAG, "totalGoAccuracy = $totalGoAccuracy\ntotalNogoAccuracy = $totalNogoAccuracy\ntotalGoAvgSpeed = $totalGoAvgSpeed s" +
                "\ntotalGoSuccesses = $totalGoSuccesses\ntotalNogoSuccesses = $totalNogoSuccesses")

        prefsEditor.apply()

        // print out new totals
//        Log.i(TAG, getTotals().toString())
        backgroundLayout.setBackgroundResource(R.color.dark_purple)
        instructionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        instructionTextView.text = getString(R.string.finished)
        mHandler.removeCallbacksAndMessages(null)
    }

    // debugging for lastTen
    private fun getResults() {
        // get the list if it exists
        fixedSizeList = if(!prefs.contains("lastTen")) {
            ArrayList<LinkedTreeMap<String, Double>>()
        } else {
            val tempStr = prefs.getString("lastTen", "")
            gson.fromJson(tempStr, ArrayList<LinkedTreeMap<String, Double>>().javaClass)
        }

        // loop through the list
        for(result in fixedSizeList) {
            // "Go" -> 1f means Go, otherwise No-Go
            if(result["Go"] == 1.0) {
                // if goSpeed is a key, then the user succeeded, otherwise the user failed
                if(result.contains("goSpeed")) {
                    val goSpeed = result["goSpeed"]
                    Log.i(TAG, "Go - Success $goSpeed")
                } else {
                    Log.i(TAG, "Go - Failed")
                }
            } else {
                // "Success" -> 1f means success, otherwise failed
                if(result["Success"] == 1.0) {
                    Log.i(TAG, "No-Go - Success")
                } else {
                    Log.i(TAG, "No-Go - Failed")
                }
            }
        }
    }

//    private fun getResultsList(): ArrayList<HashMap<String, Float>> {
//        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
//        // initializes empty arraylist of HashMaps
//        val resultsList = ArrayList<HashMap<String, Float>>()
//        // the game has never been run
//        if(preferences.contains("currTrial")) {
//            // if the game has been run at least once, currTrial >= 1
//            val curr = preferences.getInt("currTrial", 1)
//            for(i in 0 until curr) {
//                resultsList.add(HashMap<String, Float>())
//                resultsList[i] = gson.fromJson(preferences.getString("Trial$i", null), HashMap<String, Float>().javaClass)
//            }
//        }
//        return resultsList
//    }

//    private fun getTotals(): HashMap<String, Float> {
//        val resultsList = getResultsList()
//        val returnMap = HashMap<String, Float>()
//        var goAccuracy = 0f
//        var nogoAccuracy = 0f
//        var goAvgSpeed = 0f
//        var speedCount = 0
//        var goSuccesses = 0f
//        var nogoSuccesses = 0f
//
//        for(resultMap in resultsList) {
//            goAccuracy += resultMap["goAccuracy"]!!
//            nogoAccuracy += resultMap["nogoAccuracy"]!!
//            val temp = resultMap["goAvgSpeed"]!!
//            if(temp != 0f) {
//                goAvgSpeed += resultMap["goAvgSpeed"]!!
//                speedCount++
//            }
//            goSuccesses += resultMap["goSuccesses"]!!
//            nogoSuccesses += resultMap["nogoSuccesses"]!!
//        }
//        // edge case
//        if(speedCount != 0) {
//            goAvgSpeed /= speedCount
//        }
//        goAccuracy /= resultsList.size
//        nogoAccuracy /= resultsList.size
//
//        returnMap["totalGoAccuracy"] = goAccuracy
//        returnMap["totalNogoAccuracy"] = nogoAccuracy
//        returnMap["totalGoAvgSpeed"] = goAvgSpeed
//        returnMap["totalGoSuccesses"] = goSuccesses
//        returnMap["totalNogoSuccesses"] = nogoSuccesses
//        return returnMap
//    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
        mHandler.removeCallbacksAndMessages(null);
    }


    companion object {
        // tag for debugging purposes
        private const val TAG = "Project-Tag"
    }
}