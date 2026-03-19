package me.nickotato.simplePolls.managers

import me.nickotato.simplePolls.SimplePolls
import me.nickotato.simplePolls.data.PollDataStorage
import me.nickotato.simplePolls.model.Poll
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.time.LocalDateTime

object PollsManager {
    val polls = PollDataStorage.loadAllPolls()   //mutableListOf<Poll>()
    val expiredPolls = PollDataStorage.loadAllPolls(true)   //mutableListOf<Poll>()

    private var nextId = 1

    init {
        val highestActive = polls.maxOfOrNull { it.id } ?: 0
        val highestExpired = expiredPolls.maxOfOrNull { it.id } ?: 0

        nextId = maxOf(highestActive, highestExpired) + 1
    }


    fun createPoll(question: String, options: List<String>, durationSeconds: Long) {
        val endsAt = durationSeconds.let { LocalDateTime.now().plusSeconds(durationSeconds) }

        val poll = Poll(
            id = nextId++,
            question = question,
            options = options.associateWith { 0 }.toMutableMap(),
            endsAt = endsAt,
        )

        polls.add(poll)
    }

    fun beginRepeatingTasks() {
        object : BukkitRunnable() {
            override fun run() {
                checkIfPollsExpired()
            }
        }.runTaskTimer(SimplePolls.instance, 0L, 20L)

        object : BukkitRunnable() {
            override fun run() {
                save()
            }
        }.runTaskTimer(SimplePolls.instance, 0L, 20 * 20)
    }

    private fun checkIfPollsExpired() {
        val now = LocalDateTime.now()

        val iterator = polls.iterator()

        while (iterator.hasNext()) {
            val poll = iterator.next()

            if (poll.endsAt.isBefore(now)) {

                expiredPolls.add(poll)

                iterator.remove()

                val file = File(SimplePolls.instance.dataFolder, "polldata/${poll.id}.yml")
                if (file.exists()) file.delete()
            }
        }
    }


    fun setPlayersAnswer(poll: Poll, player: Player, choice: String) {
        poll.votes[player.uniqueId.toString()] = choice
        calculateOptionsVotes(poll)
    }

    private fun calculateOptionsVotes(poll: Poll) {
        for (option in poll.options.keys) {
            poll.options[option] = 0
        }

        for ((_, votedOption) in poll.votes) {
            poll.options[votedOption] = poll.options.getOrDefault(votedOption, 0) + 1
        }
    }

    fun getOptionWithMostVotes(poll: Poll): String {
        if (poll.options.isEmpty()) return "None"

        val top = poll.options.maxByOrNull { it.value } ?: return "None"
        return top.key
    }

    fun save() {
        PollDataStorage.saveAllPolls(false, polls)
        PollDataStorage.saveAllPolls(true, expiredPolls)
    }
}
