package com.zf.card

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.SimpleItemAnimator
import java.util.*

/**
 * This implementation of [RecyclerView.ItemAnimator] provides basic
 * animations on remove, add, and move events that happen to the items in
 * a RecyclerView. RecyclerView uses a DefaultItemAnimator by default.
 *
 * @see RecyclerView.setItemAnimator
 */
class CardItemAnimator : SimpleItemAnimator() {
    private val mPendingRemovals =
        ArrayList<ViewHolder>()
    private val mPendingAdditions =
        ArrayList<ViewHolder>()
    private val mPendingMoves =
        ArrayList<MoveInfo>()
    private val mPendingChanges =
        ArrayList<ChangeInfo>()
    var mAdditionsList =
        ArrayList<ArrayList<ViewHolder>>()
    var mMovesList =
        ArrayList<ArrayList<MoveInfo>>()
    var mChangesList =
        ArrayList<ArrayList<ChangeInfo>>()
    var mAddAnimations = ArrayList<ViewHolder?>()
    var mMoveAnimations = ArrayList<ViewHolder?>()
    var mRemoveAnimations = ArrayList<ViewHolder?>()
    var mChangeAnimations = ArrayList<ViewHolder?>()

    var mCardMoveY=600
    var mCardMoveOutTime=300L
    var mCardScale=0.85
    class MoveInfo internal constructor(
        var holder: ViewHolder,
        var fromX: Int,
        var fromY: Int,
        var toX: Int,
        var toY: Int
    )

    class ChangeInfo private constructor(oldHolder: ViewHolder, newHolder: ViewHolder) {
        var oldHolder: ViewHolder?
        var newHolder: ViewHolder?
        var fromX = 0
        var fromY = 0
        var toX = 0
        var toY = 0

        internal constructor(
            oldHolder: ViewHolder, newHolder: ViewHolder,
            fromX: Int, fromY: Int, toX: Int, toY: Int
        ) : this(oldHolder, newHolder) {
            this.fromX = fromX
            this.fromY = fromY
            this.toX = toX
            this.toY = toY
        }

        override fun toString(): String {
            return ("ChangeInfo{"
                    + "oldHolder=" + oldHolder
                    + ", newHolder=" + newHolder
                    + ", fromX=" + fromX
                    + ", fromY=" + fromY
                    + ", toX=" + toX
                    + ", toY=" + toY
                    + '}')
        }

        init {
            this.oldHolder = oldHolder
            this.newHolder = newHolder
        }
    }

    override fun runPendingAnimations() {
        val removalsPending = !mPendingRemovals.isEmpty()
        val movesPending = !mPendingMoves.isEmpty()
        val changesPending = !mPendingChanges.isEmpty()
        val additionsPending = !mPendingAdditions.isEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return
        }
        // First, remove stuff
        for (holder in mPendingRemovals) {
            animateRemoveImpl(holder)
        }
        mPendingRemovals.clear()
        // Next, move stuff
        if (movesPending) {
            val moves =
                ArrayList<MoveInfo>()
            moves.addAll(mPendingMoves)
            mMovesList.add(moves)
            mPendingMoves.clear()
            val mover = Runnable {
                for (moveInfo in moves) {
                    animateMoveImpl(
                        moveInfo.holder, moveInfo.fromX, moveInfo.fromY,
                        moveInfo.toX, moveInfo.toY
                    )
                }
                moves.clear()
                mMovesList.remove(moves)
            }
            if (removalsPending) {
                val view = moves[0].holder.itemView
                ViewCompat.postOnAnimationDelayed(
                    view,
                    mover,
                    removeDuration
                )
            } else {
                mover.run()
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            val changes =
                ArrayList<ChangeInfo>()
            changes.addAll(mPendingChanges)
            mChangesList.add(changes)
            mPendingChanges.clear()
            val changer = Runnable {
                for (change in changes) {
                    animateChangeImpl(change)
                }
                changes.clear()
                mChangesList.remove(changes)
            }
            if (removalsPending) {
                val holder = changes[0].oldHolder
                ViewCompat.postOnAnimationDelayed(
                    holder!!.itemView,
                    changer,
                    removeDuration
                )
            } else {
                changer.run()
            }
        }
        // Next, add stuff
        if (additionsPending) {
            val additions = ArrayList<ViewHolder>()
            additions.addAll(mPendingAdditions)
            mAdditionsList.add(additions)
            mPendingAdditions.clear()
            val adder = Runnable {
                for (holder in additions) {
                    animateAddImpl(holder)
                }
                additions.clear()
                mAdditionsList.remove(additions)
            }
            if (removalsPending || movesPending || changesPending) {
                val removeDuration = if (removalsPending) removeDuration else 0
                val moveDuration = if (movesPending) moveDuration else 0
                val changeDuration = if (changesPending) changeDuration else 0
                val totalDelay =
                    removeDuration + Math.max(moveDuration, changeDuration)
                val view = additions[0].itemView
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay)
            } else {
                adder.run()
            }
        }
    }

    override fun animateRemove(holder: ViewHolder): Boolean {
        resetAnimation(holder)
        mPendingRemovals.add(holder)
        return true
    }

    private fun animateRemoveImpl(holder: ViewHolder) {
        val view = holder.itemView
        val animation = view.animate()
        mRemoveAnimations.add(holder)
        animation.setDuration(removeDuration).alpha(0f).setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchRemoveStarting(holder)
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    view.alpha = 1f
                    dispatchRemoveFinished(holder)
                    mRemoveAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }).start()
    }

    override fun animateAdd(holder: ViewHolder): Boolean {
        resetAnimation(holder)
        holder.itemView.alpha = 0f
        mPendingAdditions.add(holder)
        return true
    }

    fun animateAddImpl(holder: ViewHolder) {
        val view = holder.itemView
        val animation = view.animate()
        mAddAnimations.add(holder)
        animation.alpha(1f).setDuration(addDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchAddStarting(holder)
                }

                override fun onAnimationCancel(animator: Animator) {
                    view.alpha = 1f
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    dispatchAddFinished(holder)
                    mAddAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }).start()
    }

    override fun animateMove(
        holder: ViewHolder, fromx: Int, fromy: Int,
        tox: Int, toY: Int
    ): Boolean {
        var fromX = fromx
        var fromY = fromy
        var toX = tox
        val view = holder.itemView
        fromX += holder.itemView.translationX.toInt()
        fromY += holder.itemView.translationY.toInt()
        resetAnimation(holder)
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        if (toY == 0) {
            toX = fromX
        } else {
            /*  if (deltaX != 0) {
                view.setTranslationX(-deltaX);
            }*/
        }

        mPendingMoves.add(
            MoveInfo(
                holder,
                fromX,
                fromY,
                toX,
                toY
            )
        )
        return true
    }

    fun animateMoveImpl(
        holder: ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaY != 0) {
            if (toY == 0) {
                view.pivotX = view.pivotX + 15
                view.animate().translationY(-deltaY + mCardMoveY.toFloat())
                    .scaleX( mCardScale.toFloat())
                    .scaleY( mCardScale.toFloat())
                    .setDuration(mCardMoveOutTime)
                    .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animator: Animator) {

                    }

                    override fun onAnimationCancel(animator: Animator) {

                    }

                    override fun onAnimationEnd(animator: Animator) {
                        startMoveBack(deltaX, view, deltaY, holder)
                    }
                }).start()
            } else {
                view.animate().translationY(-deltaY.toFloat())
                    .setDuration(mCardMoveOutTime)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animator: Animator) {

                        }

                        override fun onAnimationCancel(animator: Animator) {

                        }

                        override fun onAnimationEnd(animator: Animator) {
                            startMoveBack(deltaX, view, deltaY, holder)
                        }
                    }).start()
            }
        }

    }

    private fun startMoveBack(
        deltaX: Int,
        view: View,
        deltaY: Int,
        holder: ViewHolder
    ) {
        if (deltaX != 0) {
            view.animate().translationX(0f)
        }
        if (deltaY != 0) {
            view.animate().translationY(0f)
        }
        // TODO: make EndActions end listeners instead, since end actions aren't called when
        // vpas are canceled (and can't end them. why?)
        // need listener functionality in VPACompat for this. Ick.
        val animation = view.animate()
        mMoveAnimations.add(holder)
        animation.setDuration(moveDuration).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animator: Animator) {
                dispatchMoveStarting(holder)
            }

            override fun onAnimationCancel(animator: Animator) {
                if (deltaX != 0) {
                    view.translationX = 0f
                }
                if (deltaY != 0) {
                    view.translationY = 0f
                }
            }

            override fun onAnimationEnd(animator: Animator) {
                animation.setListener(null)
                view.scaleX = 1f
                view.scaleY = 1f
                dispatchMoveFinished(holder)
                mMoveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }

    override fun animateChange(
        oldHolder: ViewHolder, newHolder: ViewHolder,
        fromX: Int, fromY: Int, toX: Int, toY: Int
    ): Boolean {
        if (oldHolder === newHolder) {
            // Don't know how to run change animations when the same view holder is re-used.
            // run a move animation to handle position changes.
            return animateMove(oldHolder, fromX, fromY, toX, toY)
        }
        val prevTranslationX = oldHolder.itemView.translationX
        val prevTranslationY = oldHolder.itemView.translationY
        val prevAlpha = oldHolder.itemView.alpha
        resetAnimation(oldHolder)
        val deltaX = (toX - fromX - prevTranslationX).toInt()
        val deltaY = (toY - fromY - prevTranslationY).toInt()
        // recover prev translation state after ending animation
        oldHolder.itemView.translationX = prevTranslationX
        oldHolder.itemView.translationY = prevTranslationY
        oldHolder.itemView.alpha = prevAlpha
        if (newHolder != null) {
            // carry over translation values
            resetAnimation(newHolder)
            newHolder.itemView.translationX = -deltaX.toFloat()
            newHolder.itemView.translationY = -deltaY.toFloat()
            newHolder.itemView.alpha = 0f
        }
        mPendingChanges.add(
            ChangeInfo(
                oldHolder,
                newHolder,
                fromX,
                fromY,
                toX,
                toY
            )
        )
        return true
    }

    fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        if (view != null) {
            val oldViewAnim = view.animate().setDuration(
                changeDuration
            )
            mChangeAnimations.add(changeInfo.oldHolder)
            oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX.toFloat())
            oldViewAnim.translationY(changeInfo.toY - changeInfo.fromY.toFloat())
            oldViewAnim.alpha(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchChangeStarting(changeInfo.oldHolder, true)
                }

                override fun onAnimationEnd(animator: Animator) {
                    oldViewAnim.setListener(null)
                    view.alpha = 1f
                    view.translationX = 0f
                    view.translationY = 0f
                    dispatchChangeFinished(changeInfo.oldHolder, true)
                    mChangeAnimations.remove(changeInfo.oldHolder)
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
        if (newView != null) {
            val newViewAnimation = newView.animate()
            mChangeAnimations.add(changeInfo.newHolder)
            newViewAnimation.translationX(0f).translationY(0f).setDuration(changeDuration)
                .alpha(1f).setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animator: Animator) {
                        dispatchChangeStarting(changeInfo.newHolder, false)
                    }

                    override fun onAnimationEnd(animator: Animator) {
                        newViewAnimation.setListener(null)
                        newView.alpha = 1f
                        newView.translationX = 0f
                        newView.translationY = 0f
                        dispatchChangeFinished(changeInfo.newHolder, false)
                        mChangeAnimations.remove(changeInfo.newHolder)
                        dispatchFinishedWhenDone()
                    }
                }).start()
        }
    }

    private fun endChangeAnimation(
        infoList: MutableList<ChangeInfo>,
        item: ViewHolder
    ) {
        for (i in infoList.indices.reversed()) {
            val changeInfo =
                infoList[i]
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo)
                }
            }
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder)
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder)
        }
    }

    private fun endChangeAnimationIfNecessary(
        changeInfo: ChangeInfo,
        item: ViewHolder?
    ): Boolean {
        var oldItem = false
        if (changeInfo.newHolder === item) {
            changeInfo.newHolder = null
        } else if (changeInfo.oldHolder === item) {
            changeInfo.oldHolder = null
            oldItem = true
        } else {
            return false
        }
        item!!.itemView.alpha = 1f
        item.itemView.translationX = 0f
        item.itemView.translationY = 0f
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun endAnimation(item: ViewHolder) {
        val view = item.itemView
        // this will trigger end callback which should set properties to their target values.
        view.animate().cancel()
        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (i in mPendingMoves.indices.reversed()) {
            val moveInfo =
                mPendingMoves[i]
            if (moveInfo.holder === item) {
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(item)
                mPendingMoves.removeAt(i)
            }
        }
        endChangeAnimation(mPendingChanges, item)
        if (mPendingRemovals.remove(item)) {
            view.alpha = 1f
            dispatchRemoveFinished(item)
        }
        if (mPendingAdditions.remove(item)) {
            view.alpha = 1f
            dispatchAddFinished(item)
        }
        for (i in mChangesList.indices.reversed()) {
            val changes =
                mChangesList[i]
            endChangeAnimation(changes, item)
            if (changes.isEmpty()) {
                mChangesList.removeAt(i)
            }
        }
        for (i in mMovesList.indices.reversed()) {
            val moves =
                mMovesList[i]
            for (j in moves.indices.reversed()) {
                val moveInfo =
                    moves[j]
                if (moveInfo.holder === item) {
                    view.translationY = 0f
                    view.translationX = 0f
                    dispatchMoveFinished(item)
                    moves.removeAt(j)
                    if (moves.isEmpty()) {
                        mMovesList.removeAt(i)
                    }
                    break
                }
            }
        }
        for (i in mAdditionsList.indices.reversed()) {
            val additions = mAdditionsList[i]
            if (additions.remove(item)) {
                view.alpha = 1f
                dispatchAddFinished(item)
                if (additions.isEmpty()) {
                    mAdditionsList.removeAt(i)
                }
            }
        }

        // animations should be ended by the cancel above.
        check(!(mRemoveAnimations.remove(item) && DEBUG)) {
            ("after animation is cancelled, item should not be in "
                    + "mRemoveAnimations list")
        }
        check(!(mAddAnimations.remove(item) && DEBUG)) {
            ("after animation is cancelled, item should not be in "
                    + "mAddAnimations list")
        }
        check(!(mChangeAnimations.remove(item) && DEBUG)) {
            ("after animation is cancelled, item should not be in "
                    + "mChangeAnimations list")
        }
        check(!(mMoveAnimations.remove(item) && DEBUG)) {
            ("after animation is cancelled, item should not be in "
                    + "mMoveAnimations list")
        }
        dispatchFinishedWhenDone()
    }

    private fun resetAnimation(holder: ViewHolder) {
        if (sDefaultInterpolator == null) {
            sDefaultInterpolator = ValueAnimator().interpolator
        }
        holder.itemView.animate().interpolator = sDefaultInterpolator
        endAnimation(holder)
    }

    override fun isRunning(): Boolean {
        return (!mPendingAdditions.isEmpty()
                || !mPendingChanges.isEmpty()
                || !mPendingMoves.isEmpty()
                || !mPendingRemovals.isEmpty()
                || !mMoveAnimations.isEmpty()
                || !mRemoveAnimations.isEmpty()
                || !mAddAnimations.isEmpty()
                || !mChangeAnimations.isEmpty()
                || !mMovesList.isEmpty()
                || !mAdditionsList.isEmpty()
                || !mChangesList.isEmpty())
    }

    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call [.dispatchAnimationsFinished] to notify any
     * listeners.
     */
    fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    override fun endAnimations() {
        var count = mPendingMoves.size
        for (i in count - 1 downTo 0) {
            val item =
                mPendingMoves[i]
            val view = item.holder.itemView
            view.translationY = 0f
            view.translationX = 0f
            dispatchMoveFinished(item.holder)
            mPendingMoves.removeAt(i)
        }
        count = mPendingRemovals.size
        for (i in count - 1 downTo 0) {
            val item = mPendingRemovals[i]
            dispatchRemoveFinished(item)
            mPendingRemovals.removeAt(i)
        }
        count = mPendingAdditions.size
        for (i in count - 1 downTo 0) {
            val item = mPendingAdditions[i]
            item.itemView.alpha = 1f
            dispatchAddFinished(item)
            mPendingAdditions.removeAt(i)
        }
        count = mPendingChanges.size
        for (i in count - 1 downTo 0) {
            endChangeAnimationIfNecessary(mPendingChanges[i])
        }
        mPendingChanges.clear()
        if (!isRunning) {
            return
        }
        var listCount = mMovesList.size
        for (i in listCount - 1 downTo 0) {
            val moves =
                mMovesList[i]
            count = moves.size
            for (j in count - 1 downTo 0) {
                val moveInfo =
                    moves[j]
                val item = moveInfo.holder
                val view = item.itemView
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(moveInfo.holder)
                moves.removeAt(j)
                if (moves.isEmpty()) {
                    mMovesList.remove(moves)
                }
            }
        }
        listCount = mAdditionsList.size
        for (i in listCount - 1 downTo 0) {
            val additions = mAdditionsList[i]
            count = additions.size
            for (j in count - 1 downTo 0) {
                val item = additions[j]
                val view = item.itemView
                view.alpha = 1f
                dispatchAddFinished(item)
                additions.removeAt(j)
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions)
                }
            }
        }
        listCount = mChangesList.size
        for (i in listCount - 1 downTo 0) {
            val changes =
                mChangesList[i]
            count = changes.size
            for (j in count - 1 downTo 0) {
                endChangeAnimationIfNecessary(changes[j])
                if (changes.isEmpty()) {
                    mChangesList.remove(changes)
                }
            }
        }
        cancelAll(mRemoveAnimations)
        cancelAll(mMoveAnimations)
        cancelAll(mAddAnimations)
        cancelAll(mChangeAnimations)
        dispatchAnimationsFinished()
    }

    fun cancelAll(viewHolders: List<ViewHolder?>) {
        for (i in viewHolders.indices.reversed()) {
            viewHolders[i]!!.itemView.animate().cancel()
        }
    }

    /**
     * {@inheritDoc}
     *
     *
     * If the payload list is not empty, DefaultItemAnimator returns `true`.
     * When this is the case:
     *
     *  * If you override [.animateChange], both
     * ViewHolder arguments will be the same instance.
     *
     *  *
     * If you are not overriding [.animateChange],
     * then DefaultItemAnimator will call [.animateMove] and
     * run a move animation instead.
     *
     *
     */
    override fun canReuseUpdatedViewHolder(
        viewHolder: ViewHolder,
        payloads: List<Any>
    ): Boolean {
        return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads)
    }

    companion object {
        private const val DEBUG = false
        private var sDefaultInterpolator: TimeInterpolator? = null
    }
}