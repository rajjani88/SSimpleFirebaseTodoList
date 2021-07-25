package com.easy.todolist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easy.todolist.databinding.ActivityHomeBinding
import com.easy.todolist.datamodel.TaskModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class HomeActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        //is user is alreay Login
        if (auth.currentUser == null) {
            goToLogin()
        }
        var currentUser = auth.currentUser

        loadAllData(currentUser!!.uid.toString())

        binding.btnAdd.setOnClickListener {
            var task = binding.etTask.text.toString().trim()

            if (task.isEmpty()) {
                binding.etTask.setError("Task Can not be empty!")
                return@setOnClickListener
            }

            val taskData = TaskModel(task, false, currentUser!!.uid.toString())
            db.collection("all_tasks")
                .add(taskData)
                .addOnSuccessListener {
                    Toast.makeText(this@HomeActivity, "Task Saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@HomeActivity, "Task Not Saved", Toast.LENGTH_SHORT).show()
                    Log.e("HA", "Error saving : Err :" + it.message)
                }


        }

        //swipe refresh
        binding.refresh.setOnRefreshListener {
            if (binding.refresh.isRefreshing) {
                binding.refresh.isRefreshing = false
            }
            loadAllData(currentUser!!.uid)
        }


        binding.btnLogout.setOnClickListener {
            //for logout
            auth.signOut()
            goToLogin()
        }
    }

    fun goToLogin() {
        startActivity(Intent(this@HomeActivity, MainActivity::class.java))
        finish()
    }

    //for laoding all task from server
    fun loadAllData(userID: String) {

        val taksList = ArrayList<TaskModel>()

        var ref = db.collection("all_tasks")
        ref.whereEqualTo("userID", userID)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    Toast.makeText(this@HomeActivity, "No Task Found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                for (doc in it) {
                    val taskModel = doc.toObject(TaskModel::class.java)
                    taksList.add(taskModel)
                }

                binding.rvToDoList.apply {
                    layoutManager =
                        LinearLayoutManager(this@HomeActivity, RecyclerView.VERTICAL, false)
                    adapter = TaskAdapter(taksList, this@HomeActivity)
                }

            }

    }

}





