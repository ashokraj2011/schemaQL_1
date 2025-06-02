from flask import Flask, jsonify, request

app = Flask(__name__)

@app.route('/v1/customers', methods=['GET'])
def get_customer():
    print("i am called by some ")
    #customer_id = request.args.get('customer_id')


    return jsonify({
        "data": {
            "profile": {
                "customer_id": "1",
                "name": "Alice"
            },
            "metrics":{
                "loyaltyScore":100
            }
        }
    })

if __name__ == '__main__':
    app.run(port=5001, debug=True)
