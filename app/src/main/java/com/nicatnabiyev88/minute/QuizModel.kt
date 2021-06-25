package com.nicatnabiyev88.minute

class QuizModel (var question:String, var answerA:String, var answerB: String, var answerC: String, var correctAnswer: String){
    companion object{
         var quizList = ArrayList<QuizModel>()
    }
}