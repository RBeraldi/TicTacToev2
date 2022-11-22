from flask import Flask,request
from flask_restful import Resource, Api
import json,random

app = Flask(__name__)
api = Api(app)

class myHUB(Resource):

    def get(self):
        board = json.loads(request.args.get('chessboard'))

        agentMoves = []
        for i in range(9):
            if (board[i]==0): agentMoves.append(i)
        random.shuffle(agentMoves)
        if len(agentMoves)<=0:
            return 0
        else:
            return agentMoves[0]


api.add_resource(myHUB, '/')

if __name__ == '__main__':
    print('starting myHUB api...waiting')
