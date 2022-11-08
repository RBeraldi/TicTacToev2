package com.labmacc.tictactoev2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

var winner = FREE
var chess = IntArray(9) { FREE }

class ChessView(context: Context?) :  View(context), View.OnTouchListener {

    val yourTurnMessage="You Move"
    val agentTurnMessage="Agent Move"
    val waitingMessage="Waiting for the agent"

    var message = yourTurnMessage

    var waitingAgent = false
    val remoteAgent = RemoteAgent().retrofit.create(PostChess::class.java)

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
    suspend fun agentMove() //Call a remote agent
    {
      // agentMoveLocal();return
      //Send the chessboardboard configuration

      val reply = remoteAgent.doGet(JSONArray(chess))

      Log.i("REPLY",reply.body().toString())
      chess[reply.body()!!.toInt()]= AGENT


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
                    }
                    winner=FREE
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
                if ((winner==AGENT) or (winner==USER)) return true

                chess[k] = USER
                checkwinner()
                invalidate()
                if (winner==USER) return true

                GlobalScope.launch(Dispatchers.IO)
                {
                    message=agentTurnMessage
                    waitingAgent=true
                    agentMove()
                    waitingAgent=false
                    message=yourTurnMessage
                    checkwinner()
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
    fun checkwinner() //check if someone wins
    {
        Log.i(TAG,"CHECK WINNER...")
        var a = 0
        var b = 0
        var c = 0
        Log.i("WINNER",""
                +chess[0]
                +" "+chess[1]
                +" "+chess[2]
                +" "+chess[3]
                +" "+chess[4]
                +" "+chess[5]
                +" "+chess[6]
                +" "+chess[7]
                +" "+chess[8]
        )
        winner = FREE
        //check horizontal lines
        for (k in 0..8) {
            if (chess[k] == USER) a += 1
            if (chess[k] == AGENT) b += 1
            if (chess[k] != FREE) c += 1
            if ((k == 2) or (k == 5) or (k == 8)) {
                if (a == 3) {
                    winner = USER;return
                }
                if (b == 3) {
                    winner = AGENT;return
                }
                a = 0;b = 0
            }
            print(" ciao")
            Log.i("WINNER","Agent: "+b)
            if (c == 9) { winner = FAIR;return}
        }
        //check vertical lines
        for (offset in 0..2)
            for (k in listOf(0, 3, 6)) {
                if (chess[k + offset] == USER) a += 1
                if (chess[k + offset] == AGENT) b += 1
                if (a == 3) {
                    winner = USER;return
                }
                if (b == 3) {
                    winner = AGENT;return
                }
                if (k == 6) {
                    a = 0;b = 0
                }
            }
        //check diagonals
        for (k in listOf(0, 4, 8)) {
            if (chess[k] == USER) a += 1
            if (chess[k] == AGENT) b += 1
            if (a == 3) {
                winner = USER;return
            }
            if (b == 3) {
                winner = AGENT;return
            }
        }
        a = 0;b = 0
        for (k in listOf(2, 4, 6)) {
            if (chess[k] == USER) a += 1
            if (chess[k] == AGENT) b += 1
            if (a == 3) {
                winner = USER;return
            }
            if (b == 3) {
                winner = AGENT;return
            }
        }
    }
}