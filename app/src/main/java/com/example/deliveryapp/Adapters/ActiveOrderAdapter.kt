package com.example.deliveryapp.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deliveryapp.Models.ActiveOrderModel
import com.example.deliveryapp.OrderActivity
import com.example.deliveryapp.databinding.OrderItemBinding

class ActiveOrderAdapter(
    val context: Context,
    val list:ArrayList<ActiveOrderModel>
): RecyclerView.Adapter<ActiveOrderAdapter.ActiveViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActiveOrderAdapter.ActiveViewHolder {
        val binding=OrderItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return ActiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActiveOrderAdapter.ActiveViewHolder, position: Int) {
        val listModel=list[position]
        holder.orderId.text = listModel.getOrderId().toString()
        holder.orderTotalPrice.text=listModel.getOrderTotalPrice()
        holder.orderStatus.text=listModel.getOrderStatus()

        holder.item.setOnClickListener{
            val intent= Intent(context,OrderActivity::class.java)
            intent.putExtra("orderId",listModel.getOrderId())
            intent.putExtra("orderStatus",listModel.getOrderStatus())
            intent.putExtra("orderTotalPrice",listModel.getOrderTotalPrice())
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
    class ActiveViewHolder(binding:OrderItemBinding):RecyclerView.ViewHolder(binding.root) {
        val orderId=binding.activeOrdersId
        val orderTotalPrice=binding.activeOrdersTotal
        val orderStatus=binding.activeOrdersStatus

        val item=binding.root


    }
}

