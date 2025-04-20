package com.example.deliveryapp.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deliveryapp.Models.ActiveOrderModel
import com.example.deliveryapp.OrderActivity
import com.example.deliveryapp.databinding.OrderItemBinding
import android.util.Log

class ActiveOrderAdapter(
    val context: Context,
    val list:ArrayList<ActiveOrderModel>
): RecyclerView.Adapter<ActiveOrderAdapter.ActiveViewHolder>() {
    private val TAG = "ActiveOrderAdapter"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActiveOrderAdapter.ActiveViewHolder {
        val binding=OrderItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return ActiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActiveOrderAdapter.ActiveViewHolder, position: Int) {
        val model = list[position]
        
        // Логируем данные о модели
        Log.d(TAG, "onBindViewHolder: модель #$position: ID=${model.getId()}, цена=${model.getTotalPrice()} (${model.getTotalPrice().javaClass.simpleName})")
        
        holder.apply {
            orderId.text = "Заказ №${model.getId()}"
            
            // Используем метод getFormattedPrice() для отображения цены
            orderTotalPrice.text = model.getFormattedPrice()
            Log.d(TAG, "onBindViewHolder: Установлена отформатированная цена ${model.getFormattedPrice()} для заказа ID=${model.getId()}")
            
            // Отображаем статус заказа
            orderStatus.text = when (model.getOrderStatus()) {
                "inprocess" -> "В процессе"
                "delivered" -> "Доставлен"
                else -> model.getOrderStatus()
            }
            
            // Обработчик нажатия на карточку заказа
            item.setOnClickListener {
                val intent = Intent(context, OrderActivity::class.java)
                intent.putExtra("orderId", model.getId())
                intent.putExtra("orderTotalPrice", model.getFormattedPrice())
                intent.putExtra("orderStatus", model.getOrderStatus())
                intent.putExtra("address", model.getAddress())
                intent.putExtra("date", model.getCreatedAt())
                context.startActivity(intent)
            }
        }
    }
    
    // Функция для форматирования суммы с пробелами
    private fun formatPrice(price: Int): String {
        Log.d("ActiveOrderAdapter", "Formatting price: $price")
        if (price <= 0) return "0 ₽"
        
        val formattedPrice = price.toString()
            .reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()
        
        Log.d("ActiveOrderAdapter", "Formatted price: $formattedPrice")
        return "$formattedPrice ₽"
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

