package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.localization.AppLanguage
import com.example.localization.S
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.KhawlaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: KhawlaViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    val formulas by viewModel.formulas.collectAsState()
    val mistakes by viewModel.mistakes.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Flashcards, 1: Formulas, 2: Mistakes
    var isAddCardOpen by remember { mutableStateOf(false) }
    var isAddFormulaOpen by remember { mutableStateOf(false) }
    var isAddMistakeOpen by remember { mutableStateOf(false) }

    val currentStudyCardIndex by viewModel.currentStudyCardIndex.collectAsState()
    val isAnswerRevealed by viewModel.isAnswerRevealed.collectAsState()

    val dueCards = flashcards.filter { it.nextReviewTimestamp <= System.currentTimeMillis() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(S.get("tab_flashcards", language), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(S.get("tab_formulas", language), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(S.get("tab_mistakes", language), fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> FlashcardsTabContent(
                dueCards = dueCards,
                allCards = flashcards,
                subjects = subjects,
                language = language,
                currentIndex = currentStudyCardIndex,
                isRevealed = isAnswerRevealed,
                onReveal = { viewModel.revealFlashcardAnswer() },
                onRate = { card, rating -> viewModel.rateFlashcard(card, rating) },
                onDelete = { viewModel.deleteFlashcard(it) },
                onOpenAdd = { isAddCardOpen = true }
            )
            1 -> FormulasTabContent(
                formulas = formulas,
                subjects = subjects,
                language = language,
                onConvertToCard = { viewModel.convertFormulaToFlashcard(it) },
                onDelete = { viewModel.deleteFormula(it) },
                onOpenAdd = { isAddFormulaOpen = true }
            )
            2 -> MistakesTabContent(
                mistakes = mistakes,
                subjects = subjects,
                language = language,
                onToggleReviewed = { viewModel.toggleMistakeReviewed(it) },
                onDelete = { viewModel.deleteMistake(it) },
                onOpenAdd = { isAddMistakeOpen = true }
            )
        }
    }

    if (isAddCardOpen) {
        AddFlashcardDialog(
            language = language,
            subjects = subjects,
            onDismiss = { isAddCardOpen = false },
            onAdd = { q, a, subId, tags ->
                viewModel.addFlashcard(q, a, subId, tags)
                isAddCardOpen = false
            }
        )
    }

    if (isAddFormulaOpen) {
        AddFormulaDialog(
            language = language,
            subjects = subjects,
            onDismiss = { isAddFormulaOpen = false },
            onAdd = { name, expr, expl, ex, subId, tags ->
                viewModel.addFormula(name, expr, expl, ex, subId, tags)
                isAddFormulaOpen = false
            }
        )
    }

    if (isAddMistakeOpen) {
        AddMistakeDialog(
            language = language,
            subjects = subjects,
            onDismiss = { isAddMistakeOpen = false },
            onAdd = { title, desc, cat, lesson, subId ->
                viewModel.addMistake(title, desc, cat, lesson, subId)
                isAddMistakeOpen = false
            }
        )
    }
}

@Composable
fun FlashcardsTabContent(
    dueCards: List<FlashcardEntity>,
    allCards: List<FlashcardEntity>,
    subjects: List<SubjectEntity>,
    language: AppLanguage,
    currentIndex: Int,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onRate: (FlashcardEntity, String) -> Unit,
    onDelete: (FlashcardEntity) -> Unit,
    onOpenAdd: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
    ) {
        // Study Card Box if there are due cards
        if (dueCards.isNotEmpty()) {
            val safeIndex = currentIndex.coerceIn(0, dueCards.size - 1)
            val currentCard = dueCards[safeIndex]
            val subject = subjects.find { it.id == currentCard.subjectId }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${S.get("tab_flashcards", language)} (${safeIndex + 1}/${dueCards.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            if (subject != null) {
                                val name = if (language == AppLanguage.ARABIC) subject.nameAr else subject.nameFr
                                Surface(shape = RoundedCornerShape(8.dp), color = PalePink) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepLavender,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = currentCard.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        if (!isRevealed) {
                            Button(
                                onClick = onReveal,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PalePink),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = S.get("show_answer", language),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepLavender
                                )
                            }
                        } else {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftCardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = currentCard.answer,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3-choice rating buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onRate(currentCard, "forgot") },
                                    colors = ButtonDefaults.buttonColors(containerColor = GentleError.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = S.get("rating_forgot", language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GentleError,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { onRate(currentCard, "hard") },
                                    colors = ButtonDefaults.buttonColors(containerColor = GentleWarning.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = S.get("rating_hard", language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GentleWarning,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { onRate(currentCard, "easy") },
                                    colors = ButtonDefaults.buttonColors(containerColor = GentleSuccess.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = S.get("rating_easy", language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GentleSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EmptyStateView(
                        icon = Icons.Default.DoneAll,
                        message = S.get("cards_empty", language)
                    )
                }
            }
        }

        // Header for all cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${S.get("tab_flashcards", language)} (${allCards.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onOpenAdd) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        items(allCards, key = { it.id }) { card ->
            val sub = subjects.find { it.id == card.subjectId }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = card.question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onDelete(card) }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FormulasTabContent(
    formulas: List<FormulaEntity>,
    subjects: List<SubjectEntity>,
    language: AppLanguage,
    onConvertToCard: (FormulaEntity) -> Unit,
    onDelete: (FormulaEntity) -> Unit,
    onOpenAdd: () -> Unit
) {
    var convertedFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${S.get("tab_formulas", language)} (${formulas.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = onOpenAdd,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PalePink)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = S.get("add_formula", language), color = DeepLavender)
                }
            }
        }

        convertedFeedback?.let { feedback ->
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GentleSuccess.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = feedback,
                        style = MaterialTheme.typography.labelMedium,
                        color = GentleSuccess,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (formulas.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Functions,
                    message = S.get("no_data", language)
                )
            }
        } else {
            items(formulas, key = { it.id }) { f ->
                val sub = subjects.find { it.id == f.subjectId }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = f.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { onDelete(f) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Math expression in distinct styled banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = f.mathExpression,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = DeepLavender,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (f.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = f.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (f.example.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ex: ${f.example}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GentleSuccess
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (sub != null) {
                                val name = if (language == AppLanguage.ARABIC) sub.nameAr else sub.nameFr
                                Text(text = name, style = MaterialTheme.typography.labelSmall, color = MutedText)
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            TextButton(
                                onClick = {
                                    onConvertToCard(f)
                                    convertedFeedback = S.get("converted_success", language)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FlipToFront, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = S.get("convert_to_card", language), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MistakesTabContent(
    mistakes: List<MistakeEntity>,
    subjects: List<SubjectEntity>,
    language: AppLanguage,
    onToggleReviewed: (MistakeEntity) -> Unit,
    onDelete: (MistakeEntity) -> Unit,
    onOpenAdd: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${S.get("tab_mistakes", language)} (${mistakes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = onOpenAdd,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PalePink)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = S.get("add_mistake", language), color = DeepLavender)
                }
            }
        }

        if (mistakes.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.EditNote,
                    message = S.get("no_data", language)
                )
            }
        } else {
            items(mistakes, key = { it.id }) { m ->
                val sub = subjects.find { it.id == m.subjectId }
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (m.isReviewed) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = m.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDelete(m) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(16.dp))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GentleError.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = S.get(m.category, language),
                                style = MaterialTheme.typography.labelSmall,
                                color = GentleError,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (m.description.isNotBlank()) {
                            Text(
                                text = m.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (m.lessonLearned.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftCardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = GentleSuccess, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = m.lessonLearned,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MainText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = m.isReviewed,
                                onClick = { onToggleReviewed(m) },
                                label = {
                                    Text(if (m.isReviewed) S.get("status_understood", language) else S.get("status_needs_review", language))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (m.isReviewed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )

                            if (sub != null) {
                                val name = if (language == AppLanguage.ARABIC) sub.nameAr else sub.nameFr
                                Text(text = name, style = MaterialTheme.typography.labelSmall, color = MutedText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddFlashcardDialog(
    language: AppLanguage,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onAdd: (q: String, a: String, subId: Long, tags: String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = S.get("add_flashcard", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text(S.get("card_question", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text(S.get("card_answer", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(S.get("cancel", language)) }
                    Button(
                        onClick = { if (question.isNotBlank() && answer.isNotBlank()) onAdd(question, answer, selectedSubjectId, "") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(S.get("save", language))
                    }
                }
            }
        }
    }
}

@Composable
fun AddFormulaDialog(
    language: AppLanguage,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onAdd: (name: String, expr: String, expl: String, ex: String, subId: Long, tags: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var expr by remember { mutableStateOf("") }
    var expl by remember { mutableStateOf("") }
    var ex by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = S.get("add_formula", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(S.get("formula_name", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = expr,
                    onValueChange = { expr = it },
                    label = { Text(S.get("formula_expr", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = expl,
                    onValueChange = { expl = it },
                    label = { Text(S.get("formula_explanation", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ex,
                    onValueChange = { ex = it },
                    label = { Text(S.get("formula_example", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(S.get("cancel", language)) }
                    Button(
                        onClick = { if (name.isNotBlank() && expr.isNotBlank()) onAdd(name, expr, expl, ex, selectedSubjectId, "") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(S.get("save", language))
                    }
                }
            }
        }
    }
}

@Composable
fun AddMistakeDialog(
    language: AppLanguage,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onAdd: (title: String, desc: String, cat: String, lesson: String, subId: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var lesson by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("cat_forgot_formula") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }

    val categories = listOf("cat_forgot_formula", "cat_calc_error", "cat_misunderstood", "cat_stuck", "cat_other")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = S.get("add_mistake", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(S.get("mistake_title", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Text(text = S.get("mistake_category", language), style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(S.get(cat, language), style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PalePink)
                        )
                    }
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(S.get("task_desc", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lesson,
                    onValueChange = { lesson = it },
                    label = { Text(S.get("mistake_lesson", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(S.get("cancel", language)) }
                    Button(
                        onClick = { if (title.isNotBlank()) onAdd(title, desc, category, lesson, selectedSubjectId) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(S.get("save", language))
                    }
                }
            }
        }
    }
}
