package com.ethran.notable.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ethran.notable.R
import com.ethran.notable.classes.AppRepository
import com.ethran.notable.components.BreadCrumb
import com.ethran.notable.modals.FolderConfigDialog
import com.ethran.notable.components.PageMenu
import com.ethran.notable.components.PagePreview
import com.ethran.notable.TAG
import com.ethran.notable.components.Topbar
import com.ethran.notable.db.Folder
import com.ethran.notable.db.Notebook
import com.ethran.notable.db.Page
import com.ethran.notable.modals.AppSettings
import com.ethran.notable.modals.AppSettingsModal
import com.ethran.notable.modals.NotebookConfigDialog
import com.ethran.notable.utils.noRippleClickable
import com.ethran.notable.utils.isLatestVersion
import compose.icons.FeatherIcons
import compose.icons.feathericons.FilePlus
import compose.icons.feathericons.Folder
import compose.icons.feathericons.FolderPlus
import compose.icons.feathericons.Settings
import io.shipbook.shipbooksdk.Log
import kotlin.concurrent.thread

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun Library(navController: NavController, folderId: String? = null) {
    val context = LocalContext.current

    var isSettingsOpen by remember {
        mutableStateOf(false)
    }
    val appRepository = AppRepository(LocalContext.current)

    val books by appRepository.bookRepository.getAllInFolder(folderId).observeAsState()
    val singlePages by appRepository.pageRepository.getSinglePagesInFolder(folderId)
        .observeAsState()
    val folders by appRepository.folderRepository.getAllInFolder(folderId).observeAsState()

    var isLatestVersion by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(key1 = Unit, block = {
        thread {
            isLatestVersion = isLatestVersion(context, true)
        }
    })

    var showFloatingEditor by remember { mutableStateOf(false) }
    var floatingEditorPageId by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize()
    ) {
        Topbar {
            Row(Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                BadgedBox(
                    badge = {
                        if (!isLatestVersion) Badge(
                            backgroundColor = Color.Black,
                            modifier = Modifier.offset(-12.dp, 10.dp)
                        )
                    }
                ) {
                    Icon(
                        imageVector = FeatherIcons.Settings,
                        contentDescription = "",
                        Modifier
                            .padding(8.dp)
                            .noRippleClickable {
                                isSettingsOpen = true
                            })
                }
            }
            Row(
                Modifier
                    .padding(10.dp)
            ) {
                BreadCrumb(folderId) { navController.navigate("library" + if (it == null) "" else "?folderId=${it}") }
            }
//           I do not know what the idea behind it was
//            // Add the new "Floating Editor" button here
//            Text(text = "Floating Editor",
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .noRippleClickable {
//                        val page = Page(
//                            notebookId = null,
//                            parentFolderId = folderId,
//                            nativeTemplate = appRepository.kvProxy.get(
//                                "APP_SETTINGS", AppSettings.serializer()
//                            )?.defaultNativeTemplate ?: "blank"
//                        )
//                        appRepository.pageRepository.create(page)
//                        floatingEditorPageId = page.id
//                        showFloatingEditor = true
//                    }
//                    .padding(10.dp))

        }

        Column(
            Modifier.padding(10.dp)
        ) {

            Spacer(Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    // Add new folder row
                    Row(
                        Modifier
                            .noRippleClickable {
                                val folder = Folder(
                                    parentFolderId = folderId,
                                    title = context.getString(R.string.home__new_folder)
                                )
                                appRepository.folderRepository.create(folder)
                            }
                            .border(0.5.dp, Color.Black)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = FeatherIcons.FolderPlus,
                            contentDescription = "Add Folder Icon",
                            Modifier.height(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(text = context.getString(R.string.home__add_new_folder))
                    }
                }
                if (folders?.isNotEmpty() == true) {
                    items(folders!!) { folder ->
                        var isFolderSettingsOpen by remember { mutableStateOf(false) }
                        if (isFolderSettingsOpen) FolderConfigDialog(
                            folderId = folder.id,
                            onClose = {
                                Log.i(TAG, "Closing Directory Dialog")
                                isFolderSettingsOpen = false
                            })
                        Row(
                            Modifier
                                .combinedClickable(
                                    onClick = {
                                        navController.navigate("library?folderId=${folder.id}")
                                    },
                                    onLongClick = {
                                        isFolderSettingsOpen = !isFolderSettingsOpen
                                    },
                                )
                                .border(0.5.dp, Color.Black)
                                .padding(10.dp, 5.dp)
                        ) {
                            Icon(
                                imageVector = FeatherIcons.Folder,
                                contentDescription = "folder icon",
                                Modifier.height(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(text = folder.title)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(text = context.getString(R.string.home__quick_pages))
            Spacer(Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Add the "Add quick page" button
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(3f / 4f)
                            .border(1.dp, Color.Gray, RectangleShape)
                            .noRippleClickable {
                                val page = Page(
                                    notebookId = null,
                                    parentFolderId = folderId,
                                    nativeTemplate = appRepository.kvProxy.get(
                                        "APP_SETTINGS", AppSettings.serializer()
                                    )?.defaultNativeTemplate ?: "blank"
                                )
                                appRepository.pageRepository.create(page)
                                navController.navigate("pages/${page.id}")
                            }
                    ) {
                        Icon(
                            imageVector = FeatherIcons.FilePlus,
                            contentDescription = "Add Quick Page",
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
                // Render existing pages
                if (singlePages?.isNotEmpty() == true) {
                    items(singlePages!!.reversed()) { page ->
                        val pageId = page.id
                        var isPageSelected by remember { mutableStateOf(false) }
                        Box {
                            PagePreview(
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("pages/$pageId")
                                        },
                                        onLongClick = {
                                            isPageSelected = true
                                        },
                                    )
                                    .width(100.dp)
                                    .aspectRatio(3f / 4f)
                                    .border(1.dp, Color.Black, RectangleShape),
                                pageId = pageId
                            )
                            if (isPageSelected) PageMenu(
                                pageId = pageId,
                                canDelete = true,
                                onClose = { isPageSelected = false })
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(text = context.getString(R.string.home__notebooks))
            Spacer(Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Add the "Add quick page" button
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(3f / 4f)
                            .border(1.dp, Color.Gray, RectangleShape)
                            .noRippleClickable {
                                appRepository.bookRepository.create(
                                    Notebook(
                                        parentFolderId = folderId,
                                        defaultNativeTemplate = appRepository.kvProxy.get(
                                            "APP_SETTINGS", AppSettings.serializer()
                                        )?.defaultNativeTemplate ?: "blank"
                                    )
                                )
                            }
                    ) {
                        Icon(
                            imageVector = FeatherIcons.FilePlus,
                            contentDescription = "Add Quick Page",
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
                if (books?.isNotEmpty() == true) {
                    items(books!!.reversed()) { item ->
                        var isSettingsOpen by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f)
                                .border(1.dp, Color.Black, RectangleShape)
                                .background(Color.White)
                                .clip(RoundedCornerShape(2))
                        ) {
                            Box {
                                val pageId = item.pageIds[0]
                                PagePreview(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(3f / 4f)
                                        .border(1.dp, Color.Black, RectangleShape)
                                        .combinedClickable(
                                            onClick = {
                                                val bookId = item.id
                                                val pageId = item.openPageId ?: item.pageIds[0]
                                                navController.navigate("books/$bookId/pages/$pageId")
                                            },
                                            onLongClick = {
                                                isSettingsOpen = true
                                            },
                                        ), pageId
                                )
                            }
                            Text(
                                text = item.pageIds.size.toString(),
                                modifier = Modifier
                                    .background(Color.Black)
                                    .padding(5.dp),
                                color = Color.White
                            )
                            Text(
                                text = item.title,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp) // Add some padding above the row
                                    .background(Color.White)
                            )
                        }
                        if (isSettingsOpen) NotebookConfigDialog(
                            bookId = item.id,
                            onClose = { isSettingsOpen = false })
                    }
                }
            }
        }
    }

    if (isSettingsOpen) AppSettingsModal(onClose = { isSettingsOpen = false })

    // Add the FloatingEditorView here
    if (showFloatingEditor && floatingEditorPageId != null) {
        FloatingEditorView(
            navController = navController,
            pageId = floatingEditorPageId!!,
            onDismissRequest = {
                showFloatingEditor = false
                floatingEditorPageId = null
            }
        )
    }
}



