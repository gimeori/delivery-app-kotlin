package com.example.deliveryapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.deliveryapp.Network.LoginRepository
import com.example.deliveryapp.Network.UserUpdateRequest
import com.example.deliveryapp.R
import com.example.deliveryapp.StartActivity
import com.example.deliveryapp.LoginActivity
import com.example.deliveryapp.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import android.widget.Toast
import com.example.deliveryapp.Network.TokenManager
import android.app.Dialog
import android.widget.TextView
import com.example.deliveryapp.Models.CourierStatsModel

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var loginRepository: LoginRepository
    private lateinit var tokenManager: TokenManager
    private val TAG = "ProfileFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginRepository = LoginRepository(requireContext())
        tokenManager = TokenManager.getInstance(requireContext())
    }

    @SuppressLint("MissingInflatedId")
    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editTextName)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.editTextPhone)
        val emailEditText = dialogView.findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.editTextPassword)
        val saveButton = dialogView.findViewById<Button>(R.id.buttonSaveChanges)
        
        // Загружаем актуальные данные пользователя для заполнения полей
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val profile = loginRepository.getUserProfile()
                
                withContext(Dispatchers.Main) {
                    // Заполняем поля актуальными данными
                    nameEditText.setText(profile.name ?: "")
                    phoneEditText.setText(profile.phone ?: "")
                    emailEditText.setText(profile.email)
                    // Поле пароля оставляем пустым
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке профиля для диалога", e)
            }
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        dialogBuilder.setTitle("Редактировать профиль")
        val dialog = dialogBuilder.create()

        saveButton.setOnClickListener {
            val newName = nameEditText.text.toString().trim()
            val newPhone = phoneEditText.text.toString().trim()
            val newEmail = emailEditText.text.toString().trim()
            val newPassword = passwordEditText.text.toString().trim()
            
            // Создаем объект запроса
            val updateRequest = UserUpdateRequest(
                name = newName.ifEmpty { null },
                phone = newPhone.ifEmpty { null },
                email = newEmail.ifEmpty { null },
                password = newPassword.ifEmpty { null } // Отправляем пароль только если он введен
            )
            
            // Вызываем API в корутине
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Попытка обновить профиль пользователя: $updateRequest")
                    val response = loginRepository.updateUserProfileMe(updateRequest)
                    
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Профиль успешно обновлен. Ответ: $response")
                        
                        // Обновляем UI
                        loadUserProfile()
                        
                        Toast.makeText(requireContext(), "Профиль обновлен!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при обновлении профиля", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Ошибка обновления: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        dialog.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Загружаем данные о пользователе
        loadUserProfile()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Устанавливаем слушатель нажатий на кнопку редактирования
        binding.profileBtnEdit.setOnClickListener {
            // Вместо старого диалога показываем новый
            showEditProfileDialog()
        }
        
        // Устанавливаем слушатель нажатий на кнопку выхода
        binding.buttonExit.setOnClickListener {
            // Показываем Toast сообщение при нажатии
            Toast.makeText(requireContext(), "Выполняется выход...", Toast.LENGTH_SHORT).show()
            
            // Принудительно выполняем локальный логаут
            Log.e("LOGOUT_DEBUG", "Выполняем принудительный локальный логаут")
            loginRepository.logoutSync()
            
            // Сразу переходим на экран логина
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
        
        // Добавляем обработчик нажатия на кнопку показа статистики
        binding.btnShowStats.setOnClickListener {
            showCourierStatsDialog()
        }
    }

    // Метод для показа статистики курьера
    private fun showCourierStatsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_courier_stats, null)
        
        // Получаем ссылки на TextView в диалоге
        val tvOrdersToday = dialogView.findViewById<TextView>(R.id.tv_orders_today)
        val tvPriceToday = dialogView.findViewById<TextView>(R.id.tv_price_today)
        val tvOrdersMonth = dialogView.findViewById<TextView>(R.id.tv_orders_month)
        val tvPriceMonth = dialogView.findViewById<TextView>(R.id.tv_price_month)
        val tvOrdersAllTime = dialogView.findViewById<TextView>(R.id.tv_orders_all_time)
        val tvPriceAllTime = dialogView.findViewById<TextView>(R.id.tv_price_all_time)
        val btnClose = dialogView.findViewById<Button>(R.id.btn_close_stats)
        
        // Создаем диалог
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        
        // Кнопка закрытия
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        // Показываем диалог
        dialog.show()
        
        // Загружаем данные
        loadCourierStats(tvOrdersToday, tvPriceToday, tvOrdersMonth, tvPriceMonth, tvOrdersAllTime, tvPriceAllTime)
    }
    
    // Метод для загрузки статистики с сервера
    private fun loadCourierStats(
        tvOrdersToday: TextView, 
        tvPriceToday: TextView,
        tvOrdersMonth: TextView,
        tvPriceMonth: TextView,
        tvOrdersAllTime: TextView,
        tvPriceAllTime: TextView
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Запрос статистики курьера")
                val stats = loginRepository.getCourierStats()
                
                withContext(Dispatchers.Main) {
                    updateStatsUI(stats, tvOrdersToday, tvPriceToday, tvOrdersMonth, tvPriceMonth, tvOrdersAllTime, tvPriceAllTime)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при получении статистики курьера", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки статистики: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    // Обновление UI с данными статистики
    private fun updateStatsUI(
        stats: CourierStatsModel,
        tvOrdersToday: TextView, 
        tvPriceToday: TextView,
        tvOrdersMonth: TextView,
        tvPriceMonth: TextView,
        tvOrdersAllTime: TextView,
        tvPriceAllTime: TextView
    ) {
        tvOrdersToday.text = stats.ordersDeliveredToday.toString()
        tvPriceToday.text = stats.ordersTotalPriceToday.toString()
        tvOrdersMonth.text = stats.ordersDeliveredLastMonth.toString()
        tvPriceMonth.text = stats.ordersTotalPriceLastMonth.toString()
        tvOrdersAllTime.text = stats.ordersDeliveredAllTime.toString()
        tvPriceAllTime.text = stats.ordersTotalPriceAllTime.toString()
    }

    // Метод для загрузки данных пользователя с сервера
    private fun loadUserProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Загрузка профиля пользователя")
                val profile = loginRepository.getUserProfile()
                
                withContext(Dispatchers.Main) {
                    // Обновляем UI с полученными данными
                    updateProfileUI(profile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке профиля пользователя", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки данных профиля: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Метод для обновления UI с данными профиля
    private fun updateProfileUI(profile: com.example.deliveryapp.Models.UserProfileModel) {
        try {
            // Находим все TextViews/EditTexts в полях профиля и заполняем их данными
            val nameField = binding.profileUsername.findViewById<EditText>(android.R.id.text1)
            if (nameField != null && profile.name != null) {
                nameField.setText(profile.name)
            }
            
            val emailField = binding.profileMail.findViewById<EditText>(android.R.id.text1)
            if (emailField != null) {
                emailField.setText(profile.email)
            }
            
            val phoneField = binding.profilePhone.findViewById<EditText>(android.R.id.text1)
            if (phoneField != null && profile.phone != null) {
                phoneField.setText(profile.phone)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обновлении UI профиля", e)
        }
    }
}
