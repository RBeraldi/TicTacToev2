package com.labmacc.tictactoev2

import android.app.Application
import android.util.Log
import android.widget.Toast

object AI  {

    //Implementation of the minimax algorithm
    //For clarity, two state evaluation functions are implemented
    //that can be combined as a single function
   //AGENT_WIN=+1,AGENT_LOOSE=-1,FAIR=0

    val NONE=-1 //game in progress
    val Free=1 //For debugging purpose

    var callCount = 0 //For debugging purpose
    fun doMove (state : IntArray): Int
    //Return the best move, the move maximizes the agent's score
    {
        var score = -10
        var move = 100
        for (k in 0..8){
            if (state[k]==FREE) //evaluate the score of the move k
            {
                val newState = state.clone()
                newState[k]=AGENT
                val s = ValueUser(newState,1)
                if (s>score){
                    move = k
                    score = s
                }
                Log.i("AI", "\nFIRST EVALUATION: " + state[0] + " " + state[1] + " " + state[2] + " " + state[3] + " " + state[4] + " " + state[5] + " " + state[6] + " " + state[7] + " " + state[8] + " score --> " + score)

            }
        }
        Log.i("COUNT",""+ callCount)
        return move
    }


    fun ValueUser (state: IntArray, depth: Int): Score
    //State value under the user's point of view - minimizing
    {
        callCount++
        if (winner(state)==AGENT) return 1
        if (winner(state)==FAIR) return 0
        if (winner(state)==USER) return -1

        var score = 10

        for (i in 0..8) {
            if (state[i] == FREE) //evaluate the score of the move i
            {
                val newstate = state.clone()
                newstate[i] = USER
                val s = ValueAgent(newstate,depth+1)
                if (s<score) score =s
            }
        }
        if (depth== Free)
            Log.i("AI", "\nUSER VALUTA BOARD: " + state[0] + " " + state[1] + " " + state[2] + " " + state[3] + " " + state[4] + " " + state[5] + " " + state[6] + " " + state[7] + " " + state[8] + " score --> " + score)
        return score
    }

    fun ValueAgent (state: IntArray, depth : Int): Score
    //State value under the agent's point of view - maximizing
    {
        callCount++
        if (winner(state)==AGENT) return 1
        if (winner(state)==FAIR) return 0
        if (winner(state)==USER) return -1

        var score = -10

        for (i in 0..8) {
            if (state[i] == FREE) //evaluate the score of the move i
            {
                val newstate = state.clone()
                newstate[i] = AGENT
                val s = ValueUser(newstate,depth+1)
                if (s>score) score =s
            }
        }
        if (depth== Free)
        Log.i("AI", "\nAGENT VALUTA BOARD: " + state[0] + " " + state[1] + " " + state[2] + " " + state[3] + " " + state[4] + " " + state[5] + " " + state[6] + " " + state[7] + " " + state[8] + " score --> " + score)
        return score
    }

    fun winner(conf : IntArray) : Int{

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
                if ((conf[winningConf[i][0]]==who)&&
                    (conf[winningConf[i][1]]==who)&&
                    (conf[winningConf[i][2]]==who)
                )
                { return who}
            }
        var draw=true
        for (i in 0..8){
            draw=draw  and (conf[i]!=FREE)
        }
        if (draw) return FAIR
        return NONE
    }
}

typealias Score = Int