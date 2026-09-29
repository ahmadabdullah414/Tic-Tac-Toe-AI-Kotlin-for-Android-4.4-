package com.game.tiktactoe.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.google.android.material.button.MaterialButton
import com.game.tiktactoe.R

object GameDialogs {

    private fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    /**
     * Builds the shared rounded/glowing result-style dialog used for both round results
     * and tournament results, mirroring the Dialog styling from the original app.
     */
    fun buildDialog(
        context: Context,
        iconRes: Int,
        accentColor: Int,
        iconSizeDp: Int = 48,
        title: String,
        message: String,
        extraContent: android.view.View? = null,
        buttonLabel: String,
        buttonColor: Int,
        buttonIcon: Int? = null,
        onButtonClick: () -> Unit
    ): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(context.dp(24), context.dp(24), context.dp(24), context.dp(24))
            background = Shapes.roundedRect(
                context, 24f,
                Shapes.colorRes(context, R.color.color_dialog_bg),
                accentColor, 2f
            )
        }

        val iconWrap = FrameLayout(context).apply {
            background = Shapes.circle(context, Shapes.colorWithAlpha(accentColor, 0.2f))
        }
        val iconView = ImageView(context).apply {
            setImageResource(iconRes)
            setPadding(context.dp(12))
        }
        iconWrap.addView(iconView, ViewGroup.LayoutParams(context.dp(iconSizeDp + 24), context.dp(iconSizeDp + 24)))
        card.addView(iconWrap)

        val titleView = TextView(context).apply {
            text = title
            setTextColor(Shapes.colorRes(context, R.color.color_text_primary))
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        card.addView(titleView, marginTopParams(context, 18))

        val messageView = TextView(context).apply {
            text = message
            setTextColor(Shapes.colorRes(context, R.color.color_text_secondary))
            textSize = 15f
            gravity = Gravity.CENTER
        }
        card.addView(messageView, marginTopParams(context, 14))

        if (extraContent != null) {
            card.addView(extraContent, marginTopParams(context, 18).apply { width = LinearLayout.LayoutParams.MATCH_PARENT })
        }

        val button = MaterialButton(context).apply {
            text = buttonLabel
            textSize = 15f
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            backgroundTintList = android.content.res.ColorStateList.valueOf(buttonColor)
            setTextColor(0xFFFFFFFF.toInt())
            cornerRadius = context.dp(12)
            if (buttonIcon != null) {
                icon = androidx.core.content.ContextCompat.getDrawable(context, buttonIcon)
                iconTint = android.content.res.ColorStateList.valueOf(0xFFFFFFFF.toInt())
                iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            }
            setOnClickListener {
                dialog.dismiss()
                onButtonClick()
            }
        }
        val buttonParams = marginTopParams(context, 22)
        buttonParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        buttonParams.height = context.dp(48)
        card.addView(button, buttonParams)

        val outerParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        val margin = context.dp(28)
        outerParams.leftMargin = margin
        outerParams.rightMargin = margin

        val outer = FrameLayout(context)
        outer.addView(card, outerParams)

        dialog.setContentView(outer)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return dialog
    }

    /** Small pill-style badge, e.g. "PlayerX: 2 - PlayerO: 1" used as extra content in result dialogs. */
    fun buildScoreBadge(context: Context, text: String, subText: String? = null): android.view.View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(context.dp(16), context.dp(12), context.dp(16), context.dp(12))
            background = Shapes.roundedRect(context, 12f, Shapes.colorRes(context, R.color.color_surface))
        }
        container.addView(TextView(context).apply {
            this.text = text
            setTextColor(Shapes.colorRes(context, R.color.color_text_primary))
            typeface = Typeface.DEFAULT_BOLD
            textSize = 14f
            gravity = Gravity.CENTER
        })
        if (subText != null) {
            container.addView(TextView(context).apply {
                this.text = subText
                setTextColor(Shapes.colorRes(context, R.color.color_text_tertiary))
                textSize = 12f
                gravity = Gravity.CENTER
            }, marginTopParams(context, 6))
        }
        return container
    }

    /** Final-score "VS" box used in the tournament-over dialog. */
    fun buildFinalScoreBox(
        context: Context,
        name1: String,
        score1: Int,
        name2: String,
        score2: Int
    ): android.view.View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
            background = Shapes.roundedRect(
                context, 12f,
                Shapes.colorRes(context, R.color.color_surface),
                Shapes.colorRes(context, R.color.color_divider), 1f
            )
        }
        container.addView(TextView(context).apply {
            text = context.getString(R.string.final_score)
            setTextColor(Shapes.colorRes(context, R.color.color_text_secondary))
            typeface = Typeface.DEFAULT_BOLD
            textSize = 13f
            gravity = Gravity.CENTER
        })

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val columnParams = LinearLayout.LayoutParams(context.dp(110), LinearLayout.LayoutParams.WRAP_CONTENT)
        row.addView(scoreColumn(context, name1, score1, Shapes.colorRes(context, R.color.brand_purple)), columnParams)
        row.addView(TextView(context).apply {
            text = context.getString(R.string.vs)
            setTextColor(Shapes.colorRes(context, R.color.color_text_tertiary))
            textSize = 13f
            gravity = Gravity.CENTER
            val p = context.dp(16)
            setPadding(p, 0, p, 0)
        })
        row.addView(scoreColumn(context, name2, score2, Shapes.colorRes(context, R.color.brand_orange)), LinearLayout.LayoutParams(columnParams))
        container.addView(row, marginTopParams(context, 12))

        return container
    }

    private fun scoreColumn(context: Context, name: String, score: Int, color: Int): android.view.View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(TextView(context).apply {
                text = name
                setTextColor(Shapes.colorRes(context, R.color.color_text_tertiary))
                textSize = 12f
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(TextView(context).apply {
                text = score.toString()
                setTextColor(color)
                typeface = Typeface.DEFAULT_BOLD
                textSize = 22f
                gravity = Gravity.CENTER
            }, marginTopParams(context, 4))
        }
    }

    fun buildConfirmDialog(
        context: Context,
        title: String,
        message: String,
        cancelLabel: String,
        confirmLabel: String,
        onConfirm: () -> Unit
    ): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(context.dp(24), context.dp(24), context.dp(24), context.dp(24))
            background = Shapes.roundedRect(
                context, 24f,
                Shapes.colorRes(context, R.color.color_dialog_bg),
                Shapes.colorRes(context, R.color.brand_red), 2f
            )
        }

        val iconWrap = FrameLayout(context).apply {
            background = Shapes.circle(context, Shapes.colorWithAlpha(Shapes.colorRes(context, R.color.brand_red), 0.2f))
        }
        val icon = ImageView(context).apply {
            setImageResource(R.drawable.ic_warning)
            setPadding(context.dp(12))
        }
        iconWrap.addView(icon, ViewGroup.LayoutParams(context.dp(72), context.dp(72)))
        card.addView(iconWrap)

        card.addView(TextView(context).apply {
            text = title
            setTextColor(Shapes.colorRes(context, R.color.color_text_primary))
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }, marginTopParams(context, 18))

        card.addView(TextView(context).apply {
            text = message
            setTextColor(Shapes.colorRes(context, R.color.color_text_secondary))
            textSize = 15f
            gravity = Gravity.CENTER
        }, marginTopParams(context, 14))

        val buttonRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val cancelButton = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = cancelLabel
            isAllCaps = false
            cornerRadius = context.dp(12)
            setOnClickListener { dialog.dismiss() }
        }
        val confirmButton = MaterialButton(context).apply {
            text = confirmLabel
            isAllCaps = false
            cornerRadius = context.dp(12)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Shapes.colorRes(context, R.color.brand_red))
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener {
                dialog.dismiss()
                onConfirm()
            }
        }
        buttonRow.addView(cancelButton, LinearLayout.LayoutParams(0, context.dp(46), 1f))
        buttonRow.addView(android.view.View(context), LinearLayout.LayoutParams(context.dp(12), 1))
        buttonRow.addView(confirmButton, LinearLayout.LayoutParams(0, context.dp(46), 1f))
        card.addView(buttonRow, marginTopParams(context, 22).apply { width = LinearLayout.LayoutParams.MATCH_PARENT })

        val outer = FrameLayout(context)
        val outerParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        val margin = context.dp(28)
        outerParams.leftMargin = margin
        outerParams.rightMargin = margin
        outer.addView(card, outerParams)

        dialog.setContentView(outer)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return dialog
    }

    private fun marginTopParams(context: Context, topDp: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = context.dp(topDp) }
    }
}
