package me.tingri.j8.streaming;

/**
 * Created by sandeep on 4/23/16.
 */
class Transaction {
    int productId;
    int sales;
    Month month;

    Transaction(int productId, Month month, int sales) {
        this.sales = sales;
        this.productId = productId;
        this.month = month;
    }

    int sales() {
        return sales;
    }

    public String toString() {
        return (" For product Id " + this.productId + " and Month " + this.month.toString() + " Sales = " + this.sales);
    }
}
