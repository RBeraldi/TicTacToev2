import json
import random

from flask import Flask, jsonify, request
from flask_restful import Resource, Api

app = Flask(__name__)
api = Api(app)

NONE = -1  # game in progress
FREE = 0
USER = 1
AGENT = 2
FAIR = 0

class myHUB(Resource):

    def get(self):
        board = json.loads(request.args.get('chessboard'))
        agentMoves = []
        for i in range(8):
            if (board[i] == 0): agentMoves.append(i)
        AI.winner(board)
        random.shuffle(agentMoves)
        #return agentMoves[0]
        return AI.doMove(AI, board)


class AI:

    def doMove(self, state: list):
        global AGENT, FREE
        # Return the best move, the move maximizes the agent's score
        score = -10
        move = 100
        for k in range(9):
            if state[k] == FREE:  # evaluate the score of the move k
                newState = state.copy()
                newState[k] = AGENT
                s = self.ValueUser(self,newState)
                if s > score:
                    move = k
                    score = s
        return move

    @staticmethod
    def ValueUser(self, state: list):
        # State value under the user's point of view - minimizing
        global AGENT, FAIR, NONE, USER
        if self.winner(state) == AGENT: return 1
        if self.winner(state) == FAIR: return 0
        if self.winner(state) == USER: return -1

        score = 10

        for i in range(9):
            if state[i] == FREE:
                newstate = state.copy()
                newstate[i] = USER
                s = self.ValueAgent(self,newstate)
                if s < score: score = s

        return score

    def ValueAgent(self, state):
        global AGENT, FAIR, USER
        if self.winner(state) == AGENT: return 1
        if self.winner(state) == FAIR: return 0
        if self.winner(state) == USER: return -1

        score = -10

        for i in range(9):
            if state[i] == FREE:
                newstate = state.copy()
                newstate[i] = AGENT
                s = self.ValueUser(self,newstate)
                if s > score: score = s
        return score

    @staticmethod
    def winner(conf: list):
        global USER, AGENT, FAIR, NONE

        winningConf = [[0, 1, 2], [3, 4, 5],
                       [6, 7, 8],
                       [0, 3, 6],
                       [1, 4, 7],
                       [2, 5, 8],
                       [0, 4, 8],
                       [2, 4, 6]]

        for who in (USER, AGENT):
            for i in range(8):
                if conf[winningConf[i][0]] == who and conf[winningConf[i][1]] == who and conf[
                    winningConf[i][2]] == who: return who

        draw = True
        for i in range(9):
            draw = draw and conf[i] != FREE
            print('prova'+str(i)+str(conf[i]))
        if draw: return FAIR
        return NONE


api.add_resource(myHUB, '/')

if __name__ == '__main__':
    print('starting myHUB api...waiting')

    app.run(host='192.168.1.248', port=8080, debug=True)
