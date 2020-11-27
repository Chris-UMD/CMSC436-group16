package com.example.apollo

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.util.*
import kotlin.math.roundToInt

class Play : AppCompatActivity() {

    lateinit var instructionTextView: TextView
    lateinit var backgroundLayout: ConstraintLayout
    lateinit var mHandler: Handler
    lateinit var prefs: SharedPreferences

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play)

        // hide title bar
        supportActionBar!!.hide()

        // debugging purposes, clears the sharedpreference file
//        clearPreferences()
//        val temp = getResultsList()
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

        // set up the initial wait screen
        instructionTextView = findViewById(R.id.instruction_text)
        backgroundLayout = findViewById(R.id.background_layout)
        backgroundLayout.setBackgroundResource(R.color.dark_purple)
        instructionTextView.setTextColor(Color.WHITE)
        instructionTextView.text = getString(R.string.wait_string)

        // set up onClickListener for the Constraint Layout
        backgroundLayout.setOnClickListener{
            if(finished) {
//                startActivity(Intent(applicationContext, Menu::class.java))
                finish()
            } else {
                // whether it is time to tap or not, record the data
                if(!waiting) {
                    waiting = true
                    if (tap) {
                        mHandler.removeCallbacksAndMessages(null);
                        goCount++
                        // to get the reaction time in milliseconds
                        val diffTime = (System.currentTimeMillis() - currentTime) / 1000f
                        goTotalTime += diffTime
                        Log.i(TAG, "clicked within $diffTime s")
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
            transitionToWait()
        }
    }

    private fun transitionToWait() {
        // if currIteration is greater
        if(currIteration >= iterations) {
            transitionToFinished()
            return
        }
        mHandler.postDelayed({
//            Log.i(TAG, "loading wait screen")
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
//            Log.i(TAG, "loading failed screen")
            backgroundLayout.setBackgroundResource(R.color.orange)
            instructionTextView.setTextColor(Color.WHITE)
            instructionTextView.text = getString(R.string.failure_to_tap)
            transitionToWait()
        }, tapPersistTime.toLong())
    }

    private fun transitionToFinished() {
        mHandler.postDelayed({
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
//            Log.i(TAG, "loading finish screen")
        Log.i(TAG, "goAccuracy = $goAccuracy\nnogoAccuracy = $nogoAccuracy\ngoAvgSpeed = $goAvgSpeed s" +
                "\ngoSuccesses = $goCount\nnogoSuccesses = $nogoCount")

        // individual trials being stored
        resultsMap.put("goAccuracy", goAccuracy)
        resultsMap.put("nogoAccuracy", nogoAccuracy)
        resultsMap.put("goAvgSpeed", goAvgSpeed)
        resultsMap.put("goSuccesses", goCount.toFloat())
        resultsMap.put("nogoSuccesses", nogoCount.toFloat())
        prefsEditor.putString("Trial$currTrial", gson.toJson(resultsMap))
        prefsEditor.apply()

        // print out new totals
        Log.i(TAG, getTotals().toString())
        backgroundLayout.setBackgroundResource(R.color.dark_purple)
        instructionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        instructionTextView.text = getString(R.string.finished)
        mHandler.removeCallbacksAndMessages(null);
    }

    private fun clearPreferences() {
        prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val prefsEditor = prefs.edit()
        prefsEditor.clear()
        prefsEditor.apply()
    }

    private fun getResultsList(): ArrayList<HashMap<String, Float>> {
        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        // initializes empty arraylist of HashMaps
        val resultsList = ArrayList<HashMap<String, Float>>()
        // the game has never been run
        if(preferences.contains("currTrial")) {
            // if the game has been run at least once, currTrial >= 1
            val curr = preferences.getInt("currTrial", 1)
            for(i in 0 until curr) {
                resultsList.add(HashMap<String, Float>())
                resultsList[i] = gson.fromJson(preferences.getString("Trial$i", null), HashMap<String, Float>().javaClass)
            }
        }
        return resultsList
    }

    private fun getTotals(): HashMap<String, Float> {
        val resultsList = getResultsList()
        val returnMap = HashMap<String, Float>()
        var goAccuracy = 0f
        var nogoAccuracy = 0f
        var goAvgSpeed = 0f
        var speedCount = 0
        var goSuccesses = 0f
        var nogoSuccesses = 0f

        for(resultMap in resultsList) {
            goAccuracy += resultMap["goAccuracy"]!!
            nogoAccuracy += resultMap["nogoAccuracy"]!!
            val temp = resultMap["goAvgSpeed"]!!
            if(temp != 0f) {
                goAvgSpeed += resultMap["goAvgSpeed"]!!
                speedCount++
            }
            goSuccesses += resultMap["goSuccesses"]!!
            nogoSuccesses += resultMap["nogoSuccesses"]!!
        }
        // edge case
        if(speedCount != 0) {
            goAvgSpeed /= speedCount
        }
        goAccuracy /= resultsList.size
        nogoAccuracy /= resultsList.size

        returnMap["totalGoAccuracy"] = goAccuracy
        returnMap["totalNogoAccuracy"] = nogoAccuracy
        returnMap["totalGoAvgSpeed"] = goAvgSpeed
        returnMap["totalGoSuccesses"] = goSuccesses
        returnMap["totalNogoSuccesses"] = nogoSuccesses
        return returnMap
    }

    companion object {
        // tag for debugging purposes
        private const val TAG = "Project-Tag"
    }

    // custom font: artographie_light (does not contain numbers, use Arial for numbers)

    // colors for Go and No-Go backgrounds stored in res/color/colors.xml

    // Stats to collect:
    // - avg GO response speed
    // - GO accuracy %
    // - No-Go accuracy %

}