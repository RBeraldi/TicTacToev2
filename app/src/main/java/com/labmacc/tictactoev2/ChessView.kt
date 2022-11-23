package com.labmacc.tictactoev2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.lang.Exception

var winner = FREE
var chess = IntArray(9) { FREE }

class ChessView(context: Context?) :  View(context), View.OnTouchListener {


    val yourTurnMessage="You Move"
    val agentTurnMessage="Agent Move"
    val waitingMessage="Waiting for the agent"

    var message = yourTurnMessage

    var waitingAgent = false
    val remoteAgent = RemoteAgent().retrofit.create(PostChessBoard::class.java)

    private  var remoteChessboard =
        Firebase
            .database("https://tictactoev2-334d2-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("chessboard")

    private  var remoteWinner =
        Firebase
            .database("https://tictactoev2-334d2-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("winner")

    init {
        setOnTouchListener(this)
        }

    suspend fun agentMoveLocal() //Function used for testing
    {
         delay(1000L) //Simulates the network delay
        val actions = ArrayList<Int>()
        for (k in 0..8) if (chess[k]==FREE) actions.add(k)
        if (actions.size>0) chess[actions.shuffled().first()]=AGENT

    }
    suspend fun agentMove():Boolean //Call a remote agent
    {
       chess[AI.doMove(chess)]=AGENT;return true
      // agentMoveLocal();return
      //Send the chessboard configuration
        try {
            val reply = remoteAgent.doGet(JSONArray(chess))
            Log.i("REPLY",reply.body().toString())
            chess[reply.body()!!.toInt()]= AGENT
            return true

        }
        catch (e : Exception){
            Log.i("NET","Failure..."+e.toString())
            return false
        }

    }

    override fun onTouch(p0: View?, event: MotionEvent?): Boolean
    //Update the UI based on the touch event
    {
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                if (winner!=FREE) //Start a new game
                {
                    for (i in 0..8){
                        chess[i]=FREE
                        remoteChessboard.setValue(Gson().toJson(chess).toString())
                    }
                    winner=FREE
                    remoteWinner.setValue(Gson().toJson(winner).toString())

                    message=yourTurnMessage
                    invalidate()
                    return true
                }
                if (waitingAgent) {
                    //Toast.makeText(context,"waiting for the agent",Toast.LENGTH_LONG).show()
                    message=waitingMessage
                    return true
                }//find the center of the area
                val j = (event.x / dx).toInt()
                val i = (event.y / dy).toInt()
                val k = 3 * i + j
                Log.i(TAG+k," "+k)
                if (chess[k] != FREE) return true

                chess[k] = USER
                checkwinner()
                remoteChessboard.setValue(Gson().toJson(chess).toString())
                remoteWinner.setValue(Gson().toJson(winner).toString())

                invalidate()
                if (winner!=FREE) return true

                GlobalScope.launch(Dispatchers.IO)
                {
                    message=agentTurnMessage
                    waitingAgent=true
                    val r = agentMove()
                    waitingAgent=false
                    message=yourTurnMessage
                    if (!r) {
                        message="SORRY, NO REPLY FROM AGENT.."
                        waitingAgent=true
                    }
                    checkwinner()
                    remoteChessboard.setValue(Gson().toJson(chess).toString())
                    remoteWinner.setValue(Gson().toJson(winner).toString())
                    invalidate()
                }

            }
        }

        return true
    }
    private var dx = 0f
    private var dy = 0f
    private val linepaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 10f
        style = Paint.Style.STROKE
        textSize=25f
    }
    private val textpaint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 1f
        style = Paint.Style.FILL
        textSize=100f
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        dx = width / 3f
        dy = height / 3f

        canvas?.drawRGB(255, 4, 244)
        canvas?.drawText(message, 10f,100f,textpaint)


        if (winner == USER) canvas?.drawRGB(0, 255, 0)
        if (winner == AGENT) canvas?.drawRGB(255, 0, 0)
        if (winner == FAIR) canvas?.drawRGB(255, 255, 0)

        canvas?.drawLine(dx, 0f, dx, height.toFloat(), linepaint)
        canvas?.drawLine(2 * dx, 0f, 2 * dx, height.toFloat(), linepaint)

        canvas?.drawLine(0f, dy, width.toFloat(), dy, linepaint)
        canvas?.drawLine(0f, 2 * dy, width.toFloat(), 2 * dy, linepaint)

        for (k in 0..8) {
            if (chess[k] == USER) //draw a Circle
            {
                val i = k % 3
                val j = k / 3
                // Toast.makeText(context, ""+i+" "+j, Toast.LENGTH_SHORT).show()
                canvas?.drawCircle(
                    dx * i.toFloat() + dx / 2,
                    dy * j.toFloat() + dy / 2,
                    dx / 2f,
                    linepaint
                )
            }

            if (chess[k] == AGENT) { //draw a cross
                val i = k % 3
                val j = k / 3

                canvas?.drawLine(i * dx, j * dy, (i + 1) * dx, (j + 1) * dy, linepaint)
                canvas?.drawLine((i + 1) * dx, j * dy, i * dx, (j + 1) * dy, linepaint)
                //Toast.makeText(context, ""+i+" "+j, Toast.LENGTH_SHORT).show()

            }
        }

    }
    fun checkwinner(){


        val winningConf = arrayOf(
            arrayOf(0,1,2),
            arrayOf(3,4,5),
            arrayOf(6,7,8),

            arrayOf(0,3,6),
            arrayOf(1,4,7),
            arrayOf(2,5,8),

            arrayOf(0,4,8),
            arrayOf(2,4,6),
            )

        for (who in listOf(USER,AGENT))
        for ( i in 0..7){
            if ((chess[winningConf[i][0]]==who)&&
                (chess[winningConf[i][1]]==who)&&
                (chess[winningConf[i][2]]==who)
            )
            { winner = who; return}
        }
        var draw=true
        for ( i in 0..8){
            draw=draw  and (chess[i]!=FREE)
        }
        if (draw) winner= FAIR

    }

}